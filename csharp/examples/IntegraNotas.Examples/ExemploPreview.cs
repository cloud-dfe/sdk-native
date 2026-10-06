namespace IntegraNotas.Examples;

public static class ExemploPreview
{
    public static async Task ExecutarAsync()
    {
        bool isProduction = false; // false = Homologação / Testes; true = Produção
        string token = "TOKEN";    // Substitua pelo Token (Softhouse/Emitente)
        bool debug = true;         // Se true, inclui detalhes da requisição caso ocorra erro

        try
        {
            using var servico = new IntegraClass(isProduction, token, debug);

            // Substitua pelo payload de pré-visualização do documento fiscal
            // Para aprender a montar o payload, consulte a documentação em https://integranotas.com.br/doc
            var payload = new Dictionary<string, object>();

            string resp = await servico.PreviewAsync(payload);

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
