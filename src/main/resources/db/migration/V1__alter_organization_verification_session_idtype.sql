ALTER TABLE organization_verification_session DROP CONSTRAINT organization_verification_session_id_type_check;

ALTER TABLE organization_verification_session ADD CONSTRAINT organization_verification_session_id_type_check
CHECK (id_type IN ("DL", "NIN", "vNIN", "VOTER_CARD", "PASSPORT"));