alter table givr_transaction drop constraint if exists givr_transaction_transaction_type_check;

alter table givr_transaction add constraint givr_transaction_transaction_type_check check(transaction_type in
('ORGANIZATION_PAYMENT', 'GIVR_DISBURSEMENT', 'VOLUNTEER_PAYMENT', 'INITIATIVE_CREATION'));