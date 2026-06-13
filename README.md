# CRDC Maven Snapshots

Fallback Maven repository for `central-crdc` internal SNAPSHOTs.

Created by the CRDC dev-agent when GitHub Packages publish is blocked by org-level permissions.

## Usage

Add to your `pom.xml`:

```xml
<repository>
    <id>crdc-maven-snapshots</id>
    <name>CRDC Maven Snapshots (public fallback)</name>
    <url>https://raw.githubusercontent.com/central-crdc/crdc-maven-snapshots/main</url>
    <releases><enabled>false</enabled></releases>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>
```

## Artifacts

| Artifact | Version | Source |
|---|---|---|
| `br.com.crdc.f1:f1-mod-commons-audit` | `0.1.0-SNAPSHOT` | f1-mod-commons@dev `22d4463` |

## Note

This repo is a temporary fallback while the root cause (org-level GitHub Packages `packages:write` permission) is being fixed.
Once the GitHub Packages publish works, this repo should be removed and `pom.xml` references reverted.

Root cause: [See f1-mod-integracao PR #136 comments](https://github.com/central-crdc/f1-mod-integracao/pull/136)
