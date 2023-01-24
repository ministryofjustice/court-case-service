# Maintenance scripts

## Helm deployments

If you want to check that a helm chart deploys successfully to a lower environment _without_ waiting for the full CI pipeline to complete, use `./helm-deploy.bash` rather than running the helm command directly. This not only makes it more easily reproducible but also prevents inadvertent deployment to prod, prod should _never_ bypass CI.

## Database dumps

It is sometimes useful to apply a copy of the production court-case-service data to preprod for testing purposes, in particular where complex migrations are involved. We've developed a number of scripts which will automate this process, allowing data to be copied from one environment to another. It's also possible to apply these to the same namespace for testing purposes or if you want to be able to reset data back after a deployment or similar.  The scripts make use of common tools such as `kubectl` and `jq` that you should already have installed on your machine. In order to retrieve secrets to connect to the database it will use your `kubectl` config and credentials.

⚠️ These scripts should not be used for restoring backups to the prod database, use [RDS Snapshots](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/other-topics/rds-snapshots.html) for this. 

The only two arguments that these scripts take are:
- `--origin_namespace` - This is where the data is coming from, typically prod. As such only read-only operations will apply to this namespace
- `--destination_namespace` - This is where the data is going to and where the `pg-dump-pod` will be created, typically preprod. This is a write operation so understand well what the script is doing before applying a script to a namespace with this argument. The scripts will not allow you to use a prod namespace in this argument. 

The following instructions will guide you through the process to take a copy of the dev database and apply it back to dev. This process has been developed primarily for copying prod data to preprod but by following these steps in dev you can make sure you're comfortable with it in a low risk environment.

1. Run the `take-database-dump.bash` script. This will create a temporary `pg-dump-pod` pod in the namespace, retrieve the secrets to access the database registered in that namespace then attempt to connect and dump the database to the dump-pod storage volume. 
```
./take-database-dump.bash --origin_namespace court-probation-dev --destination_namespace court-probation-dev
```
Once you've done this you should see that a pod has been created in the `--destination_namespace` which you can log into with `kubectl exec -it -- /bin/bash`. The SQL dumps will be located in the `/data/` directory. 
2. Run the `apply-database-dump.bash` script. This will connect to the `pg-dump-pod` created by the previous script, rename the existing `courtcaseservice` schema as a backup, then run the SQL files generated against the destination database.
```
./apply-database-dump.bash --destination_namespace court-probation-dev
```
Having run this script, if you [connect to the database](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/other-topics/rds-external-access.html) you will see that a timestamped backup has been created. This is the schema that was previously present and you can reset it by deleting (or renaming) the current `courtcaseservice` schema and setting the `bk` schema name to `courtcaseservice`.  

### Troubleshooting
You may get an error similiar to the following when you run `apply-database-dump.bash`:
```
psql:/data/data.sql:20816874: ERROR:  could not extend file "base/16401/86980": No space left on device
HINT:  Check free disk space.
```
As the error says, the problem here is a lack of disk space, so the best way to resolve this is to firstly delete the part-applied `courtcaseservice` schema, and if possible any other backups that are no longer needed. Doing so will usually be sufficient to allow a re-run to succeed. It's worth keeping at least one backup schema in the database for this reason, to ensure that this space can be reclaimed easily. Failing this, if you retry and delete a couple of times, RDS may eventually provision enough disk space to allow the full write to succeed. Read the documentation [here](https://aws.amazon.com/premiumsupport/knowledge-center/diskfull-error-rds-postgresql/) to understand more about how this works. Finally if you still hit issues you can manually provision extra storage for the instance by modifying the `db_allocated_storage` value [here](https://github.com/ministryofjustice/cloud-platform-environments/blob/1aabd86fa407e1a7677c6ab8de9960535a3978ec/namespaces/live.cloud-platform.service.justice.gov.uk/court-probation-preprod/resources/rds.tf#L19).
