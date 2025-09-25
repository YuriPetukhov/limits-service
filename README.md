# Limits Service

Сервис лимитов по стратегиям (policy‑driven limits). 
Базовый кейс — дневной лимит с автосбросом в 00:00, но модель расширяема: 
можно описывать дневные/недельные/месячные окна и лимиты по категориям транзакций.

---

## Технологии

- Java 21, Spring Boot 3
- Spring Web, Validation (Jakarta), springdoc-openapi (Swagger UI)
- PostgreSQL 16
- JPA/Hibernate, Liquibase
- Jackson, Lombok
- Scheduler (`@EnableScheduling`)

---

## Архитектура

- **Контроллеры**
    - `TransactionsController` — `debit`, `check`, `reversal`, `getById`.
    - `StrategyController` — создание, получение, список, деактивация, установка «дефолтной».
    - `UserStrategyController` — назначение стратегии пользователю, получение активной.

- **Сервисы**
    - `TransactionService` — конвейер шагов: `DebitHandler`, `CancelHandler`, `CheckHandler`.
    - `StrategyService` — хранение/поиск/матчинг стратегий, разбор spec/limits.
    - `UserStrategyService` — назначение и чтение привязок.
    - `MaintenanceService` — фоновый сброс окон (sweeper).

- **Сущности**
    - `strategy` — (name, version, enabled, is_default, limits_json, spec_json, dsl_text, …).
    - `user_strategy` — привязки пользователя к стратегиям (эффективные даты).
    - `limit_bucket` — «ведро» лимита по scope/окну (base_limit, remaining, interval_seconds, last_period_start, next_reset_at).
    - `limit_tx_registry` — журнал активных списаний (идемпотентность по `(user_id, tx_id)`).

- **Pipeline (пример debit)**
    1. **IdempotencyLookupStep** — проверка повторов по `(userId, txId)`.
    2. **ResolvePoliciesStep** — нормализация атрибутов, поиск подходящих стратегий.
    3. **EnforceMissBehaviorStep** — политика на «не найдено» (REJECT или fallback к дефолтной).
    4. **ResolveScopesAndWindowStep** — вычисление scope и окон по `limits_json`.
    5. **LoadUsageStep** — загрузка использованных сумм.
    6. **CheckLimitsStep** — проверка достаточности.
    7. **ReserveAndLogStep** — ensure ведра, журнал, списание, ответ.

> Рекомендация: вынести **в отдельные сервисы** «Валидацию» и «Нормализацию» входных атрибутов, чтобы их можно было переиспользовать и тестировать независимо.

---

## Модель `limits_json` (упрощённая спецификация)

Поддерживаются два формата: плоский объект (одиночное окно) и набор окон.

### Вариант A. Плоский объект (одно окно)

```json
{
  "limit": 10000,
  "periodIso": "P1D",
  "anchor": "UTC:00:00"
}
```

- `limit` — базовый лимит (BigDecimal > 0).
- `periodIso` — ISO‑период окна: `P1D`, `P1W`, `P1M` и т. п.  
  *Либо* можно использовать `periodSeconds` (см. ниже).
- `anchor` — якорь окна: `"ZoneId:HH:mm"`, напр. `"Europe/Moscow:00:00"` или `"UTC:00:00"`.

Альтернатива `periodIso`:

```json
{
  "limit": 200000,
  "periodSeconds": 2592000,
  "anchor": "Europe/Moscow:00:00"
}
```

> Если указаны **календарные периоды** (`periodIso` = `P1D`, `P1W`, `P1M`, …), в БД в колонке `interval_seconds` будет `NULL`.  
> Для «скользящих» периодов используется `periodSeconds` (в БД хранится положительное число секунд).

### Вариант B. Набор окон

```json
{
  "windows": [
    { "id": "day",   "limit": 10000,  "periodIso": "P1D", "anchor": "UTC:00:00" },
    { "id": "month", "limit": 200000, "periodIso": "P1M", "anchor": "UTC:00:00" }
  ]
}
```

- Можно комбинировать окна разной длительности.
- Для `periodSeconds` формат аналогичный: `{"id":"…","limit":…,"periodSeconds":…,"anchor":"…"}`.
- Парсер требует **`limit`** и хотя бы **`periodIso` или `periodSeconds`**.

### Пример «только дневной» (плоский)
```json
{ "limit": 15000, "periodIso": "P1D", "anchor": "UTC:00:00" }
```

---

## Модель `spec_json` (упрощённо)

```json
{
  "scopeTemplate": "user:${userId}:category:${category:-all}",
  "match": { "any": [ { "attr": "*", "op": "ALWAYS" } ] },
  "validation": { "requiredAttrs": [] }
}
```

- `scopeTemplate` — шаблон формирования ключа scope. Простая подстановка `${name}` с дефолтами `${name:-default}`.
- `match` — правила отбора стратегии под атрибуты запроса. Если пусто/не указано — матчится всегда.
- `validation.requiredAttrs` — список обязательных атрибутов (опционально).

---

## Liquibase — важные моменты схемы

- `limit_bucket.interval_seconds` может быть **NULL** (календарные окна) или > 0 (скользящие).
- Ограничения:
    - `remaining >= 0 AND remaining <= base_limit`
    - `last_period_start < next_reset_at`
- Уникальность:
    - `(user_id, scope_key)` в `limit_bucket`
    - `(user_id, tx_id)` в `limit_tx_registry` (идемпотентность)
- FK: `limit_tx_registry(user_id, scope_key)` → `limit_bucket(user_id, scope_key)`

---

## Сборка и запуск (Maven)

```bash
mvn clean package -DskipTests
java -jar target/limits-service-*.jar --spring.profiles.active=dev
```

Swagger UI: `http://localhost:8080/swagger-ui.html` (или `/swagger-ui/index.html`).

---

## Быстрый сценарий испытаний

### 1) Создать стратегию

**POST** `/api/v1/strategies`

```json
{
  "name": "GLOBAL_DAILY_MONTHLY",
  "version": 1,
  "enabled": true,
  "isDefault": true,
  "limits": {
    "windows": [
      { "id": "day",   "limit": 10000,  "periodIso": "P1D", "anchor": "UTC:00:00" },
      { "id": "month", "limit": 200000, "periodIso": "P1M", "anchor": "UTC:00:00" }
    ]
  },
  "spec": {
    "scopeTemplate": "user:${userId}:type:${type:-all}",
    "match": { "any": [ { "op": "ALWAYS" } ] },
    "validation": { "requiredAttrs": [] }
  }
}
```

> Для одиночного окна можно отправлять плоский объект в `limits`:  
> `{"limit": 15000, "periodIso":"P1D", "anchor":"UTC:00:00"}`

### 2) Назначить пользователю стратегию

**PUT** `/api/v1/users/{userId}/strategy`

```json
{
  "strategyId": 1,
  "isActive": true,
  "effectiveFrom": "2025-10-01T00:00:00Z",
  "effectiveTo": null
}
```

### 3) Провести списание

**POST** `/api/v1/transactions`

```json
{
  "userId": "1",
  "txId": "tx-001",
  "amount": 125.50,
  "attributes": { "category": "groceries", "currency": "RSD" },
  "occurredAt": "2025-09-21T12:11:29Z"
}
```

### 4) Проверить доступный остаток (без списания)

**POST** `/api/v1/transactions/check`

```json
{ "userId": "1", "amount": 1000, "attributes": {} }
```

### 5) Отменить списание (reversal)

**POST** `/api/v1/transactions/reversal`

```json
{
  "userId": "1",
  "txId": "tx-001",
  "occurredAt": "2025-09-21T12:20:00Z"
}
```

### 6) Прочитать результат операции

**GET** `/api/v1/transactions/{txId}?userId=1`

---

## Scheduler (демо)

В `application.yml` (dev) можно включить джоб сброса окон:

```yaml
limits:
  scheduler:
    cron: "0 */1 * * * *"   # раз в минуту (для теста)
    zone: "Europe/Moscow"
    batch-size: 500
```

> Для календарных окон (month/week/day) `interval_seconds` в БД = `NULL`.  
> Джоб берёт «просроченные» ведра и переталкивает окна: `last_period_start/next_reset_at`, устанавливая `remaining = base_limit`.

---

## Ошибки/коды (ProblemDetail)

- **400** — ошибки валидации/аргументов
- **404** — не найдено (стратегия, транзакция, привязка)
- **409** — конфликт/идемпотентность (дубликат `txId`, конфликт стратегии)
- **422** — бизнес‑ошибки (недостаточно лимита, политика не найдена)
- **500** — непросмотренные исключения

---

## Дальнейшие шаги

- Расширение support для календарных окон (`P1M`, `P3M`, `P1Y`) и смешанных наборов.
- Сервис **Validation** и **Normalization** как самостоятельные компоненты.
- Полноценные реверсалы (tombstones в реестре вместо удаления).
- Больше плейсхолдеров в `scopeTemplate` и функции трансформации.
- Метрики/алерты и админ‑API (просмотр/сброс ведер).
- Тесты (Testcontainers) для сквозных сценариев.
