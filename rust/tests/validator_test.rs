use integranotas_sdk::KeyValidator;

#[test]
fn test_check_key_com_chave_44_digitos() {
    let chave_original = "35230912345678000195550010000000011234567890";
    let resultado = KeyValidator::check_key(chave_original).expect("esperava sucesso");
    assert_eq!(resultado, chave_original);
    assert_eq!(resultado.len(), 44);
}

#[test]
fn test_check_key_com_chave_50_digitos() {
    let chave_original = "35230912345678000195550010000000011234567890123456";
    let resultado = KeyValidator::check_key(chave_original).expect("esperava sucesso");
    assert_eq!(resultado, chave_original);
    assert_eq!(resultado.len(), 50);
}

#[test]
fn test_check_key_com_pontuacoes_e_espacos() {
    let chave_formatada = "3523.0912.3456.7800.0195-5500.1000.0000.0112-3456.7890";
    let resultado = KeyValidator::check_key(chave_formatada).expect("esperava sucesso");
    assert_eq!(resultado.len(), 44);
    assert!(!resultado.contains('.'));
    assert!(!resultado.contains('-'));
}

#[test]
fn test_check_key_com_tamanhos_invalidos() {
    let chaves_invalidas = [
        "",
        "   ",
        "12345",
        "3523091234567800019555001000000001123456789",          // 43 dígitos
        "352309123456780001955500100000000112345678901",         // 45 dígitos
        "352309123456780001955500100000000112345678901234567",   // 51 dígitos
    ];

    for chave in chaves_invalidas {
        let err = KeyValidator::check_key(chave).expect_err("esperava erro");
        assert!(err.message.contains("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe."));
    }
}
