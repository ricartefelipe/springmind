-- O contrato ganha o tipo de chave CNPJ. O favorecido de demonstração "Mercado Central" tem um CNPJ como chave, mas
-- estava cadastrado como chave aleatória; as transferências feitas para ele carregam o mesmo tipo.
UPDATE beneficiaries SET pix_key_type = 'CNPJ' WHERE id = 'b2' AND pix_key = '11222333000181';
UPDATE transfers SET pix_key_type = 'CNPJ' WHERE pix_key = '11222333000181' AND pix_key_type = 'RANDOM';
