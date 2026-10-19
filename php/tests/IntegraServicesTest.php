<?php

require_once dirname(__DIR__) . '/IntegraServices.php';
require_once __DIR__ . '/Assert.php';

class IntegraServicesTest
{
    public function testConstantesDeUri()
    {
        Assert::equals("https://api.integranotas.com.br/v", IntegraServices::URI_PRODUCTION);
        Assert::equals("https://hom-api.integranotas.com.br/v", IntegraServices::URI_TESTS);
    }

    public function testGetErrorsQuandoCurlFalha()
    {
        $erro = IntegraServices::getErrors(false, "Could not resolve host", 0);
        Assert::notNull($erro);
        Assert::equals("Erro na Requisição cURL", $erro["message"]);
        Assert::equals("Could not resolve host", $erro["error"]);
        Assert::equals(0, $erro["http_code"]);
    }

    public function testGetErrorsQuandoRespostaVazia()
    {
        $erro = IntegraServices::getErrors("", "", 200);
        Assert::notNull($erro);
        Assert::equals("Resposta Vazia", $erro["message"]);
        Assert::equals(200, $erro["http_code"]);
    }

    public function testGetErrorsQuandoJsonInvalido()
    {
        $htmlInvalido = "<html>Erro 500</html>";
        $erro = IntegraServices::getErrors($htmlInvalido, "", 500);
        Assert::notNull($erro);
        Assert::equals("Resposta JSON Inválida", $erro["message"]);
        Assert::equals($htmlInvalido, $erro["response"]);
        Assert::equals(500, $erro["http_code"]);
    }

    public function testGetErrorsQuandoRespostaValidaRetornaNull()
    {
        $jsonValido = '{"sucesso":true,"codigo":200}';
        $erro = IntegraServices::getErrors($jsonValido, "", 200);
        Assert::null($erro);
    }
}
