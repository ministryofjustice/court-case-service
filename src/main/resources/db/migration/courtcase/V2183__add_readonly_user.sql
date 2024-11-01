-- Create a group
CREATE ROLE readonly;

-- Grant access to existing tables
GRANT USAGE ON SCHEMA ${schemas} TO readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA ${schemas} TO readonly;

-- Grant access to future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA ${schemas} GRANT SELECT ON TABLES TO readonly;

-- Create a final user with password
CREATE USER ${readonly.user} WITH PASSWORD '${readonly.password}';
GRANT readonly TO ${readonly.user};