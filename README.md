# bidrag-vedtak

![](https://github.com/navikt/bidrag-vedtak/workflows/continuous%20integration/badge.svg)
[![test build on pull request](https://github.com/navikt/bidrag-vedtak/actions/workflows/pr.yaml/badge.svg)](https://github.com/navikt/bidrag-vedtak/actions/workflows/pr.yaml)
[![release bidrag-vedtak](https://github.com/navikt/bidrag-vedtak/actions/workflows/release.yaml/badge.svg)](https://github.com/navikt/bidrag-vedtak/actions/workflows/release.yaml)

Repo for behandling av vedtak i Bidrag


#### Kjøre lokalt mot sky
For å kunne kjøre lokalt mot sky må du gjøre følgende

Åpne terminal på root mappen til `bidrag-vedtak`
Konfigurer kubectl til å gå mot kluster `dev-gcp`
```bash
# Sett cluster til dev-fss
kubectx dev-gcp
# Sett namespace til bidrag
kubens bidrag 

# -- Eller hvis du ikke har kubectx/kubens installert 
# (da må -n=bidrag legges til etter exec i neste kommando)
kubectl config use dev-gcp
```
Deretter kjør følgende kommando for å importere secrets. Viktig at filen som opprettes ikke committes til git

```bash
kubectl exec --tty deployment/bidrag-vedtak-feature printenv | grep -E 'AZURE_|_URL|SCOPE|TOPIC' > src/test/resources/application-lokal-nais-secrets.properties
```