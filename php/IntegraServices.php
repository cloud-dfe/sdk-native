<?php

class IntegraServices
{
    protected $is_production = false;
    protected $version = "1";
    protected $base_uri = "";
    protected $debug = false;

    const URI_PRODUCTION = "https://api.integranotas.com.br/v";
    const URI_TESTS = "https://hom-api.integranotas.com.br/v";

    public function __construct($is_production = false, $version = "1", $debug = false)
    {
        $this->is_production = (bool) $is_production;
        $this->version = (string) $version;
        $this->debug = (bool) $debug;

        $this->base_uri = $this->is_production ? self::URI_PRODUCTION : self::URI_TESTS;
    }

    /**
     * Define a versão da API a ser utilizada
     * @param string $version
     */
    private function setVersion($version)
    {
        $this->version = (string) $version;
    }

    /**
     * Valida a resposta do cURL sem reexecutar a requisição
     * @param string $response
     * @param string $curlError
     * @param int $httpCode
     * @return array|null
     */
    public static function getErrors($response, $curlError, $httpCode)
    {
        if ($response === false) {
            return array(
                "message" => "Erro na Requisição cURL",
                "error" => $curlError,
                "http_code" => $httpCode
            );
        }

        if (empty($response)) {
            return array(
                "message" => "Resposta Vazia",
                "http_code" => $httpCode
            );
        }

        json_decode($response);
        if (json_last_error() !== JSON_ERROR_NONE) {
            return array(
                "message" => "Resposta JSON Inválida",
                "response" => $response,
                "http_code" => $httpCode,
                "json_error" => json_last_error_msg()
            );
        }

        return null;
    }

    /**
     * Executa a requisição cURL para a API
     * @param string $method
     * @param string $endpoint
     * @param array $body
     * @param array $headers
     * @param string|null $version
     * @return mixed
     * @throws \RuntimeException
     */
    public function request(
        $method,
        $endpoint,
        array $body = array(),
        array $headers = array(),
        $version = null
    ) {
        if (!empty($version)) {
            $this->setVersion($version);
        }

        $oCurl = curl_init();
        $url = "{$this->base_uri}{$this->version}{$endpoint}";

        $options = array(
            CURLOPT_URL => $url,
            CURLOPT_IPRESOLVE => CURL_IPRESOLVE_V4,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_ENCODING => "",
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_HEADER => false,
            CURLOPT_CONNECTTIMEOUT => 60,
            CURLOPT_TIMEOUT => 60,
            CURLOPT_SSL_VERIFYHOST => $this->is_production ? 2 : 0,
            CURLOPT_SSL_VERIFYPEER => $this->is_production ? true : false,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => $method,
        );

        if (!empty($body)) {
            $options[CURLOPT_POSTFIELDS] = json_encode($body);
        }
        if (!empty($headers)) {
            $options[CURLOPT_HTTPHEADER] = $headers;
        }

        curl_setopt_array($oCurl, $options);

        // Executa uma única vez e captura os dados
        $response = curl_exec($oCurl);
        $curlError = curl_error($oCurl);
        $httpCode = curl_getinfo($oCurl, CURLINFO_HTTP_CODE);

        // No PHP 5.5 o fechamento explícito é necessário
        curl_close($oCurl);

        $error = self::getErrors($response, $curlError, $httpCode);

        if ($error !== null) {
            if ($this->debug) {
                $error["debug"] = array(
                    "method" => $method,
                    "url" => $url,
                    "body" => $body,
                    "headers" => $headers
                );
            }
            throw new \RuntimeException(json_encode($error));
        }

        $responseData = json_decode($response);

        if ($this->debug) {
            $debugData = array(
                "method" => $method,
                "url" => $url,
                "body" => $body,
                "headers" => $headers
            );

            if (is_object($responseData)) {
                $responseData->debug = $debugData;
            } elseif (is_array($responseData)) {
                $responseData["debug"] = $debugData;
            }
        }

        return $responseData;
    }
}
