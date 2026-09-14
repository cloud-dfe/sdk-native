<?php

require_once dirname(__DIR__) . '/IntegraClass.php';

$isProduction = false; // false = Homologação / Testes; true = Produção
$token = "TOKEN"; // Substitua pelo Token (Softhouse/Emitente)
$debug = true;  // Se true, inclui detalhes da requisição caso ocorra erro

try {
    $servico = new IntegraClass($isProduction, $token, $debug);

    // Substitua pelo payload de cancelamento do documento fiscal
    // Para aprender a montar o payload, consulte a documentação em https://integranotas.com.br/doc
    $payload = array();

    $resp = $servico->cancela($payload);

    echo json_encode($resp);
} catch (\Exception $e) {
    if ($e instanceof \InvalidArgumentException) {
        echo "Erro de configuração: " . $e->getMessage();
    } else {
        echo "Erro inesperado: " . $e->getMessage();
    }
}
