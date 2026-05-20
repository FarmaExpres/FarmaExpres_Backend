UPDATE product
SET expirationdate = CURRENT_DATE
WHERE expirationdate IS NULL;

ALTER TABLE product
    ALTER COLUMN expirationdate SET NOT NULL;
