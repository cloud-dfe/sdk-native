<?php

class AssertionException extends \Exception {}

class Assert
{
    public static $count = 0;

    public static function equals($expected, $actual, $message = "")
    {
        self::$count++;
        if ($expected !== $actual) {
            $msg = $message ? $message . " | " : "";
            $msg .= "Esperado: " . var_export($expected, true) . ", Obtido: " . var_export($actual, true);
            throw new AssertionException($msg);
        }
    }

    public static function true($condition, $message = "")
    {
        self::$count++;
        if ($condition !== true) {
            $msg = $message ?: "Esperado true, obtido " . var_export($condition, true);
            throw new AssertionException($msg);
        }
    }

    public static function false($condition, $message = "")
    {
        self::$count++;
        if ($condition !== false) {
            $msg = $message ?: "Esperado false, obtido " . var_export($condition, true);
            throw new AssertionException($msg);
        }
    }

    public static function null($value, $message = "")
    {
        self::$count++;
        if ($value !== null) {
            $msg = $message ?: "Esperado null, obtido " . var_export($value, true);
            throw new AssertionException($msg);
        }
    }

    public static function notNull($value, $message = "")
    {
        self::$count++;
        if ($value === null) {
            $msg = $message ?: "Esperado valor não nulo, obtido null";
            throw new AssertionException($msg);
        }
    }

    public static function contains($needle, $haystack, $message = "")
    {
        self::$count++;
        if (strpos((string)$haystack, (string)$needle) === false) {
            $msg = $message ? $message . " | " : "";
            $msg .= "A string não contém '" . $needle . "'. Conteúdo: " . substr((string)$haystack, 0, 100);
            throw new AssertionException($msg);
        }
    }

    public static function throws(callable $callback, $expectedExceptionClass = \Exception::class, $expectedMessagePart = null)
    {
        self::$count++;
        try {
            $callback();
        } catch (\Exception $e) {
            if (!($e instanceof $expectedExceptionClass)) {
                throw new AssertionException("Exceção incorreta. Esperado " . $expectedExceptionClass . ", obtido " . get_class($e));
            }
            if ($expectedMessagePart !== null && strpos($e->getMessage(), $expectedMessagePart) === false) {
                throw new AssertionException("Mensagem de erro incorreta. Esperado trecho '" . $expectedMessagePart . "', obtido '" . $e->getMessage() . "'");
            }
            return;
        }
        throw new AssertionException("Esperava lançamento da exceção " . $expectedExceptionClass . ", mas nenhuma foi lançada.");
    }
}
