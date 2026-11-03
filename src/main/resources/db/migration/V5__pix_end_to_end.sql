-- O end-to-end passa a seguir o formato do arranjo PIX (E + ISPB + data e hora UTC + 11 alfanuméricos) e nasce
-- quando o PIX é efetivado. Agendamentos ainda pendentes perdem o código antigo e recebem um novo na execução.
UPDATE transfers SET end_to_end_id = NULL WHERE status = 'SCHEDULED';

CREATE UNIQUE INDEX ux_transfers_end_to_end_id ON transfers (end_to_end_id);
