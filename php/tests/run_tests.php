<?php

require_once __DIR__ . '/Assert.php';
require_once __DIR__ . '/KeyValidationTest.php';
require_once __DIR__ . '/IntegraClassTest.php';
require_once __DIR__ . '/IntegraServicesTest.php';

$startTime = microtime(true);

$classes = array(
    'KeyValidationTest',
    'IntegraClassTest',
    'IntegraServicesTest'
);

$passed = 0;
$failed = 0;
$errors = array();

echo "\n" . str_repeat("=", 55) . "\n";
echo " IntegraNotas - Suíte de Testes Nativos PHP (Sem Composer)\n";
echo str_repeat("=", 55) . "\n\n";

foreach ($classes as $className) {
    echo ">> Executando [{$className}]:\n";
    $instance = new $className();
    $methods = get_class_methods($instance);

    foreach ($methods as $method) {
        if (strpos($method, 'test') === 0) {
            try {
                $instance->$method();
                $passed++;
                echo "  [PASS] {$method}\n";
            } catch (\Exception $e) {
                $failed++;
                echo "  [FAIL] {$method}\n";
                $errors[] = array(
                    'class' => $className,
                    'method' => $method,
                    'error' => $e->getMessage()
                );
            }
        }
    }
    echo "\n";
}

$elapsed = round((microtime(true) - $startTime) * 1000, 2);

echo str_repeat("-", 55) . "\n";
echo "Resultado Final:\n";
echo "  Total de Testes: " . ($passed + $failed) . "\n";
echo "  Aprovados:       {$passed}\n";
echo "  Falhas:          {$failed}\n";
echo "  Asserções:       " . Assert::$count . "\n";
echo "  Tempo:           {$elapsed} ms\n";
echo str_repeat("=", 55) . "\n";

if ($failed > 0) {
    echo "\nDetalhes das Falhas:\n";
    foreach ($errors as $idx => $err) {
        echo "  " . ($idx + 1) . ") {$err['class']}::{$err['method']}\n";
        echo "     Motivo: {$err['error']}\n";
    }
    echo "\n";
    exit(1);
}

echo "\nTodos os testes passaram com sucesso!\n\n";
exit(0);
