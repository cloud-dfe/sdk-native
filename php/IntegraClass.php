<?php

require_once __DIR__ . '/IntegraServices.php';

class IntegraClass
{
    /**
     * @var IntegraServices
     */
    protected $services;

    /**
     * @var string
     */
    protected $token;

    /**
     * Construtor da classe de serviço.
     *
     * @param bool $is_production Define ambiente de produção
     * @param string $token Token de acesso à API
     * @param bool $debug Ativa o modo de depuração para detalhar erros
     * @throws \InvalidArgumentException Quando o token não for informado
     */
    public function __construct($is_production = false, $token = "", $debug = false)
    {
        if (empty($token)) {
            throw new \InvalidArgumentException("Token é obrigatório.");
        }
        $this->services = new IntegraServices($is_production, "1", $debug);
        $this->token = $token;
    }

    private function getHeaders()
    {
        return array(
            "Content-Type: application/json",
            "Accept: application/json",
            "Authorization: {$this->token}",
        );
    }

    /**
     * Valida se a chave de acesso informada é válida (44 ou 50 dígitos).
     * @param string $chave
     * @throws \Exception Quando a chave não for válida
     */
    public static function checkKey($chave)
    {
        $key = preg_replace("/[^0-9]/", "", (string) $chave);
        $len = strlen($key);

        if ($len !== 44 && $len !== 50) {
            throw new \Exception("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
        }
    }

    public function cria(array $payload)
    {
        return $this->services->request("POST", "/servico", $payload, $this->getHeaders());
    }

    public function consulta($chave = "", array $payload = array())
    {
        if (empty($chave)) {
            $chave = isset($payload["chave"]) ? $payload["chave"] : "";
        }
        self::checkKey($chave);
        return $this->services->request("GET", "/servico/{$chave}", $payload, $this->getHeaders());
    }

    public function cancela(array $payload)
    {
        return $this->services->request("POST", "/servico/cancela", $payload, $this->getHeaders());
    }

    public function preview(array $payload)
    {
        return $this->services->request("POST", "/servico/preview", $payload, $this->getHeaders());
    }
}
