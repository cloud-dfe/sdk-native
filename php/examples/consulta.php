<?php

require_once dirname(__DIR__) . '/IntegraClass.php';

$isProduction = false; // false = Homologação / Testes; true = Produção
$token = "TOKEN"; // Substitua pelo Token (Softhouse/Emitente)
$debug = true;  // Se true, inclui detalhes da requisição caso ocorra erro

try {
    $servico = new IntegraClass($isProduction, $token, $debug);

    // Chave do documento fiscal (44 ou 50 dígitos)
    // Dica: Para captura contínua de notas, recomenda-se a utilização de Webhooks
    // em vez de consultas repetitivas (polling) via API.
    $chave = "35230912345678000195550010000000011234567890";

    $resp = $servico->consulta($chave);

    echo json_encode($resp);
} catch (\Exception $e) {
    if ($e instanceof \InvalidArgumentException) {
        echo "Erro de configuração: " . $e->getMessage();
    } else {
        echo "Erro inesperado: " . $e->getMessage();
    }
}
