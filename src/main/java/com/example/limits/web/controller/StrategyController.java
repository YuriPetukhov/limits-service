package com.example.limits.web.controller;

import com.example.limits.web.dto.CreateStrategyRequest;
import com.example.limits.web.dto.StrategyResponse;
import com.example.limits.application.result.OperationResult;
import com.example.limits.application.service.StrategyService;
import com.example.limits.web.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Управление политиками/стратегиями лимитов.
 * Работает со справочником strategy (name, version, flags, limits_json/dsl_text/spec_json).
 */
@RestController
@RequestMapping("${api.prefix}/strategies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Strategies", description = "CRUD операций над стратегиями лимитов (создать, деактивировать, сделать дефолтной, получить).")
public class StrategyController {

    private final StrategyService strategyService;

    @Operation(
            summary = "Создать стратегию",
            description = """
                    Регистрирует новую стратегию (name+version должны быть уникальны).
                    Можно передать limits_json для простых кейсов или dslText/specJson для сложных.
                    При успешном создании возвращает 201 Created и Location.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateStrategyRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "limits_json only",
                                            value = """
                                                    {
                                                      "name": "GLOBAL_DAILY",
                                                      "version": 2,
                                                      "enabled": true,
                                                      "isDefault": false,
                                                      "limits": {
                                                        "windows": [
                                                          { "id": "day", "limit": 15000, "periodIso": "P1D", "anchor": "UTC:00:00" }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(name = "dsl/spec", value = """
                                            {
                                              "name": "MONTHLY_BY_CATEGORY",
                                              "version": 1,
                                              "enabled": true,
                                              "isDefault": false,
                                              "dslText": "strategy MONTHLY_BY_CATEGORY v1 ...",
                                              "specJson": { "scope": "user:{userId}:category:{category|all}" }
                                            }""")
                            }
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Создано",
            headers = @Header(name = HttpHeaders.LOCATION, description = "URI созданной стратегии"),
            content = @Content(schema = @Schema(implementation = StrategyResponse.class)))
    @ApiResponse(responseCode = "409", description = "Конфликт name+version",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<StrategyResponse> create(@RequestBody @Valid CreateStrategyRequest req) {
        OperationResult<StrategyResponse> result = strategyService.create(req);
        return ApiResponses.createdOrOk(result.created(), result.resourceId(), result.payload());
    }

    @Operation(
            summary = "Получить стратегию по id",
            description = "Возвращает стратегию по идентификатору."
    )
    @ApiResponse(responseCode = "200", description = "Найдено",
            content = @Content(schema = @Schema(implementation = StrategyResponse.class)))
    @ApiResponse(responseCode = "404", description = "Не найдено",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @GetMapping("/{id}")
    public StrategyResponse getById(
            @Parameter(description = "ID стратегии") @PathVariable Long id) {
        return strategyService.getById(id);
    }

    @Operation(
            summary = "Список стратегий",
            description = "Возвращает список стратегий с опциональной фильтрацией по enabled и isDefault."
    )
    @ApiResponse(responseCode = "200", description = "Ок",
            content = @Content(schema = @Schema(implementation = StrategyResponse.class)))
    @GetMapping
    public List<StrategyResponse> list(
            @Parameter(description = "Только включённые") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Только дефолтные") @RequestParam(required = false) Boolean isDefault) {
        return strategyService.list(enabled, isDefault);
    }

    @Operation(
            summary = "Деактивировать стратегию",
            description = "Снимает флаг enabled=false. Исторические привязки к пользователям не трогаем."
    )
    @ApiResponse(responseCode = "200", description = "Ок",
            content = @Content(schema = @Schema(implementation = StrategyResponse.class)))
    @ApiResponse(responseCode = "404", description = "Стратегия не найдена",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @PostMapping("/{id}/deactivate")
    public StrategyResponse deactivate(
            @Parameter(description = "ID стратегии") @PathVariable Long id) {
        return strategyService.deactivate(id);
    }

    @Operation(
            summary = "Сделать стратегию дефолтной",
            description = "Устанавливает isDefault=true у выбранной стратегии и снимает флаг у предыдущей (гарантируется парциальным UNIQUE)."
    )
    @ApiResponse(responseCode = "200", description = "Ок",
            content = @Content(schema = @Schema(implementation = StrategyResponse.class)))
    @ApiResponse(responseCode = "404", description = "Стратегия не найдена",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @PostMapping("/{id}/set-default")
    public StrategyResponse setDefault(
            @Parameter(description = "ID стратегии") @PathVariable Long id) {
        return strategyService.setDefault(id);
    }
}
