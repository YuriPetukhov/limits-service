package com.example.limits.web.controller;

import com.example.limits.application.result.OperationResult;
import com.example.limits.application.service.TransactionService;
import com.example.limits.web.ApiResponses;
import com.example.limits.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transactions", description = "Операции по лимитам: списание и отмена (immutable ledger).")
public class TransactionController {

    private final TransactionService txService;

    @Operation(
            summary = "Провести транзакцию (debit)",
            description = "Создаёт запись списания. Идемпотентность по (userId, txId): повтор с тем же содержимым возвращает прежний результат.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionRequest.class),
                            examples = @ExampleObject(name = "debit", value = """
                    {
                      "userId": "u-123",
                      "txId": "tx-001",
                      "amount": 125.50,
                      "attributes": { "category": "groceries", "currency": "RSD" },
                      "occurredAt": "2025-09-21T12:11:29Z"
                    }""")
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Создано (Location указывает на ресурс транзакции)")
    @ApiResponse(responseCode = "200", description = "Идемпотентный повтор — возвращён прежний результат")
    @ApiResponse(responseCode = "409", description = "Конфликтующий дубликат txId")
    @ApiResponse(responseCode = "422", description = "Недостаточно доступного лимита / бизнес-валидация")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса")
    @PostMapping
    public ResponseEntity<TransactionResponse> debit(@RequestBody @Valid TransactionRequest req) {
        OperationResult<TransactionResponse> result = txService.debit(req);
        return ApiResponses.createdOrOk(result.created(), result.resourceId(), result.payload());
    }

    @Operation(
            summary = "Отмена транзакции (reversal)",
            description = "Создаёт новую запись-отмену с собственным txId и ссылкой на originalTxId. Сумма берётся из исходной операции со знаком «минус».",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CancelRequest.class),
                            examples = @ExampleObject(name = "reversal", value = """
                    {
                      "userId": "u-123",
                      "txId": "tx-001-rev",
                      "originalTxId": "tx-001",
                      "occurredAt": "2025-09-21T12:20:00Z",
                      "attributes": {}
                    }""")
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Создана запись отмены (Location указывает на ресурс отмены)")
//    @ApiResponse(responseCode = "200", description = "Идемпотентный повтор отмены — прежний результат")
    @ApiResponse(responseCode = "404", description = "Оригинальная транзакция не найдена")
    @ApiResponse(responseCode = "409", description = "Отмена уже произведена / нарушены правила отмены")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса")
    @PostMapping("/reversal")
    public ResponseEntity<CancelResponse> reversal(@RequestBody @Valid CancelRequest req) {
        OperationResult<CancelResponse> result = txService.cancel(req);
        return ApiResponses.createdOrOk(result.created(), result.resourceId(), result.payload());
    }

    @Operation(
            summary = "Получить результат транзакции",
            description = "Возвращает информацию по транзакции (debit или reversal) для ретраев/аудита."
    )
    @ApiResponse(responseCode = "200", description = "Найдено")
    @ApiResponse(responseCode = "404", description = "Транзакция не найдена")
    @GetMapping("/{txId}")
    public TransactionResponse getById(
            @Parameter(description = "Идентификатор транзакции (debit или reversal)")
            @PathVariable String txId,
            @Parameter(description = "Идентификатор пользователя (namespace для txId)")
            @RequestParam String userId
    ) {
        return txService.getById(userId, txId);
    }

    @Operation(
            summary = "Проверить доступный лимит (без списания)",
            description = "Выполняет расчёт доступного остатка на основании активной стратегии и журнала операций."
    )
    @ApiResponse(responseCode = "200", description = "Ок")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации запроса")
    @PostMapping("/check")
    public CheckLimitResponse check(@RequestBody @Valid CheckLimitRequest req) {
        return txService.check(req);
    }
}
