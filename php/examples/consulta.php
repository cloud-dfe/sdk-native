<?php

require_once dirname(__DIR__) . '/IntegraClass.php';

$isProduction = false; // false = Homologação / Testes; true = Produção
$token = "TOKEN"; // Substitua pelo Token (Softhouse/Emitente)
$debug = true;  // Se true, inclui detalhes da requisição caso ocorra erro

try {
    $servico = new IntegraClass($isProduction, $token, $debug);

    $chave = "CHAVE"; // Substitua pela chave do documento fiscal

    $resp = $servico->consulta($chave);

    echo json_encode($resp);
} catch (\Exception $e) {
    if ($e instanceof \InvalidArgumentException) {
        echo "Erro de configuração: " . $e->getMessage();
    } else {
        echo "Erro inesperado: " . $e->getMessage();
    }
}
