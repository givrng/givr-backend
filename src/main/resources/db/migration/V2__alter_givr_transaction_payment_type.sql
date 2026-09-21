ALTER TABLE givr_transaction DROP CONSTRAINT givr_transaction_transaction_type_check;

ALTER TABLE givr_transaction ADD CONSTRAINT givr_transaction_transaction_type_check
CHECK (transaction_type IN ('ORGANIZATION_PAYMENT', 'GIVR_DISBURSEMENT', 'VOLUNTEER_PAYMENT'));