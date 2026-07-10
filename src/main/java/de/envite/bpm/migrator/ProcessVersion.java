package de.envite.bpm.migrator;

import java.util.Optional;

public record ProcessVersion(int majorVersion, int minorVersion, int patchVersion) {

  public static Optional<ProcessVersion> fromString(String versionString) {
    if (versionString == null || !versionString.matches("\\d+\\.\\d+\\.\\d+")) {
      return Optional.empty();
    }

    String[] stringArray = versionString.split("\\.");
    int majorVersion = Integer.parseInt(stringArray[0]);
    int minorVersion = Integer.parseInt(stringArray[1]);
    int patchVersion = Integer.parseInt(stringArray[2]);
    return Optional.of(new ProcessVersion(majorVersion, minorVersion, patchVersion));
  }

  public boolean isOlderVersionThan(ProcessVersion processVersionToCompare) {
    return this.majorVersion < processVersionToCompare.majorVersion()
        || (this.majorVersion == processVersionToCompare.majorVersion()
            && this.minorVersion < processVersionToCompare.minorVersion())
        || (this.majorVersion == processVersionToCompare.majorVersion()
            && this.minorVersion == processVersionToCompare.minorVersion()
            && this.patchVersion < processVersionToCompare.patchVersion());
  }

  public boolean isOlderPatchThan(ProcessVersion processVersionToCompare) {
    return this.majorVersion == processVersionToCompare.majorVersion()
        && this.minorVersion == processVersionToCompare.minorVersion()
        && this.patchVersion < processVersionToCompare.patchVersion();
  }

  public boolean isOlderMinorThan(ProcessVersion processVersionToCompare) {
    return this.majorVersion == processVersionToCompare.majorVersion()
        && this.minorVersion < processVersionToCompare.minorVersion();
  }

  public boolean isOlderMajorThan(ProcessVersion processVersionToCompare) {
    return this.majorVersion < processVersionToCompare.majorVersion();
  }

  public String toVersionTag() {
    return majorVersion + "." + minorVersion + "." + patchVersion;
  }

  public boolean equals(ProcessVersion other) {
    return this.majorVersion == other.majorVersion
        && this.minorVersion == other.minorVersion
        && this.patchVersion == other.patchVersion;
  }
}
