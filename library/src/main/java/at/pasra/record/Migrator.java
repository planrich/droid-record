package at.pasra.record;

/**
 * Created by rich on 9/13/13.
 */
public interface Migrator {

    /**
     * @return the migration level of the database
     */
    long getLatestMigrationLevel();

    /**
     * Migrates the database to the specified target version.
     * This operation might be _DANGEROUS_. Use migrate() instead.
     * @param currentVersion
     * @param versionTarget
     */
    void migrate(long currentVersion, long versionTarget);

    /**
     * Migrate the database to the maximum level currently known.
     * This operation is idempotent.
     */
    void migrate();
}
