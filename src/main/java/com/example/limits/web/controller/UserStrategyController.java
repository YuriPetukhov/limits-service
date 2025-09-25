package com.example.limits.web.controller;

import com.example.limits.web.dto.AssignUserStrategyRequest;
import com.example.limits.web.dto.UserStrategyResponse;
import com.example.limits.application.result.OperationResult;
import com.example.limits.application.service.UserStrategyService;
import com.example.limits.web.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Управление привязками пользователя к стратегиям.
 * Позволяет задать/сменить активную стратегию и получить текущую.
 */
@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Strategies", description = "Привязка стратегий к пользователям (назначить активную стратегию, получить текущую).")
public class UserStrategyController {

    private final UserStrategyService userStrategyService;

    @Operation(
            summary = "Назначить пользователю стратегию",
            description = """
            Создаёт или обновляет активную привязку пользователя к стратегии.
            При необходимости можно указать effective_from / effective_to.
            Возвращает 201 для новой привязки или 200 для идемпотентного повтора.
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AssignUserStrategyRequest.class),
                            examples = @ExampleObject(value = """
                    {
                      "strategyId": 5,
                      "isActive": true,
                      "effectiveFrom": "2025-10-01T00:00:00Z",
                      "effectiveTo": null
                    }""")
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Создано",
            headers = @Header(name = HttpHeaders.LOCATION, description = "URI ресурса привязки"),
            content = @Content(schema = @Schema(implementation = UserStrategyResponse.class)))
    @ApiResponse(responseCode = "200", description = "Идемпотентный повтор / обновление без изменений",
            content = @Content(schema = @Schema(implementation = UserStrategyResponse.class)))
    @ApiResponse(responseCode = "404", description = "Стратегия не найдена",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Конфликт: уже есть другая активная стратегия (нарушение уникального индекса)",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @PutMapping("/{userId}/strategy")
    public ResponseEntity<UserStrategyResponse> assign(
            @Parameter(description = "Идентификатор пользователя") @PathVariable String userId,
            @RequestBody @Valid AssignUserStrategyRequest req
    ) {
        OperationResult<UserStrategyResponse> result = userStrategyService.assign(userId, req);
        return ApiResponses.createdOrOk(result.created(), result.resourceId(), result.payload());
    }

    @Operation(
            summary = "Получить активную стратегию пользователя",
            description = "Возвращает текущую активную стратегию (с учётом effective_from/to и isActive)."
    )
    @ApiResponse(responseCode = "200", description = "Ок",
            content = @Content(schema = @Schema(implementation = UserStrategyResponse.class)))
    @ApiResponse(responseCode = "404", description = "Активная стратегия не найдена",
            content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    @GetMapping("/{userId}/strategy")
    public UserStrategyResponse getActive(
            @Parameter(description = "Идентификатор пользователя") @PathVariable String userId) {
        return userStrategyService.getActive(userId);
    }
}
