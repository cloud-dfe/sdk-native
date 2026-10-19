<?php

require_once dirname(__DIR__) . '/IntegraClass.php';
require_once __DIR__ . '/Assert.php';

class IntegraClassTest
{
    public function testTokenObrigatorioVazioDeveLancarExcecao()
    {
        Assert::throws(function () {
            new IntegraClass(false, "");
        }, \InvalidArgumentException::class, "Token é obrigatório.");
    }

    public function testInstanciacaoComTokenValido()
    {
        $instancia = new IntegraClass(false, "MEU_TOKEN_TESTE", false);
        Assert::notNull($instancia);
        Assert::true($instancia instanceof IntegraClass);
    }

    public function testConsultaComChaveInvalidaDeveFalhar()
    {
        $instancia = new IntegraClass(false, "MEU_TOKEN_TESTE", false);
        Assert::throws(function () use ($instancia) {
            $instancia->consulta("123invalida");
        }, \Exception::class, "A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
    }

    public function testConsultaExtraiChaveDoPayloadSeVazia()
    {
        $instancia = new IntegraClass(false, "MEU_TOKEN_TESTE", false);
        // Payload com chave inválida deve disparar a exceção esperada
        Assert::throws(function () use ($instancia) {
            $instancia->consulta("", array("chave" => "chave_invalida_curta"));
        }, \Exception::class, "A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
    }
}
