namespace IntegraNotas.Examples;

public static class ExemploConsulta
{
    public static async Task ExecutarAsync()
    {
        bool isProduction = false; // false = Homologação / Testes; true = Produção
        string token = "TOKEN";    // Substitua pelo Token (Softhouse/Emitente)
        bool debug = true;         // Se true, inclui detalhes da requisição caso ocorra erro

        try
        {
            using var servico = new IntegraClass(isProduction, token, debug);

            // Chave do documento fiscal (44 ou 50 dígitos)
            // Dica: Para captura contínua de notas, recomenda-se a utilização de Webhooks
            // em vez de consultas repetitivas (polling) via API.
            string chave = "35230912345678000195550010000000011234567890";

            string resp = await servico.ConsultaAsync(chave);

            Console.WriteLine(resp);
        }
        catch (ArgumentException e)
        {
            Console.WriteLine($"Erro de configuração: {e.Message}");
        }
        catch (IntegraException e)
        {
            Console.WriteLine($"Erro na API (HTTP {e.HttpCode}): {e.Message}");
        }
        catch (Exception e)
        {
            Console.WriteLine($"Erro inesperado: {e.Message}");
        }
    }
}
