<?php

require_once dirname(__DIR__) . '/IntegraClass.php';
require_once __DIR__ . '/Assert.php';

class KeyValidationTest
{
    public function testChaveValida44Digitos()
    {
        $chave = "35230912345678000195550010000000011234567890";
        // Não deve disparar exceção
        IntegraClass::checkKey($chave);
        Assert::true(true, "Chave válida de 44 dígitos aceita com sucesso.");
    }

    public function testChaveValida50Digitos()
    {
        $chave = "35230912345678000195550010000000011234567890123456";
        // Não deve disparar exceção
        IntegraClass::checkKey($chave);
        Assert::true(true, "Chave válida de 50 dígitos aceita com sucesso.");
    }

    public function testChaveFormatadaComPontosETrafos()
    {
        $chaveFormatada = "3523.0912.3456.7800.0195-5500.1000.0000.0112-3456.7890";
        // Deve limpar caracteres não numéricos e validar 44 dígitos
        IntegraClass::checkKey($chaveFormatada);
        Assert::true(true, "Chave formatada sanitizada e validada com sucesso.");
    }

    public function testChaveCom43DigitosDeveLancarExcecao()
    {
        $chaveInvalida = "3523091234567800019555001000000001123456789"; // 43 dígitos
        Assert::throws(function () use ($chaveInvalida) {
            IntegraClass::checkKey($chaveInvalida);
        }, \Exception::class, "A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
    }

    public function testChaveCom45DigitosDeveLancarExcecao()
    {
        $chaveInvalida = "352309123456780001955500100000000112345678901"; // 45 dígitos
        Assert::throws(function () use ($chaveInvalida) {
            IntegraClass::checkKey($chaveInvalida);
        }, \Exception::class, "A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
    }

    public function testChaveVaziaDeveLancarExcecao()
    {
        Assert::throws(function () {
            IntegraClass::checkKey("");
        }, \Exception::class, "A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
    }
}
