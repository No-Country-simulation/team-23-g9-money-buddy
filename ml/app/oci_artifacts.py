from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path
from typing import Any


MODELS_DIR = Path(__file__).resolve().parents[1] / "models"


@dataclass(frozen=True)
class ArtifactSpec:
    key: str
    target: Path
    default_object_name: str
    env_var: str


DEFAULT_ARTIFACTS = (
    ArtifactSpec(
        key="financial_stability",
        target=MODELS_DIR / "modelo_financial_stability.pkl",
        default_object_name="financial-modelsmodelo_financial_stability.pkl",
        env_var="OCI_FINANCIAL_STABILITY_OBJECT",
    ),
    ArtifactSpec(
        key="transaction_classifier",
        target=MODELS_DIR / "classify-transactions.pkl",
        default_object_name="financial-modelsrandom_forest_model (champion).pkl",
        env_var="OCI_TRANSACTION_CLASSIFIER_OBJECT",
    ),
    ArtifactSpec(
        key="label_encoder_categoria",
        target=MODELS_DIR / "label_encoder_categoria.pkl",
        default_object_name="financial-modelslabel_encoder_categoria.pkl",
        env_var="OCI_LABEL_ENCODER_CATEGORIA_OBJECT",
    ),
    ArtifactSpec(
        key="scaler_categoria",
        target=MODELS_DIR / "scaler_categoria.pkl",
        default_object_name="financial-modelsscaler_categoria.pkl",
        env_var="OCI_SCALER_CATEGORIA_OBJECT",
    ),
)


def download_artifacts_if_enabled(
    *,
    artifacts: tuple[ArtifactSpec, ...] = DEFAULT_ARTIFACTS,
) -> dict[str, Any]:
    enabled = _env_bool("OCI_MODEL_DOWNLOAD_ENABLED", default=False)
    force_download = _env_bool("OCI_MODEL_FORCE_DOWNLOAD", default=False)
    state: dict[str, Any] = {
        "enabled": enabled,
        "force_download": force_download,
        "artifacts": {},
        "error": None,
    }

    if not enabled:
        for artifact in artifacts:
            state["artifacts"][artifact.key] = _artifact_state(artifact, "disabled")
        return state

    namespace = os.getenv("OCI_NAMESPACE")
    bucket_name = os.getenv("OCI_BUCKET_NAME")
    if not namespace or not bucket_name:
        raise RuntimeError("OCI_MODEL_DOWNLOAD_ENABLED=true requires OCI_NAMESPACE and OCI_BUCKET_NAME")

    try:
        import oci
    except ImportError as exc:
        raise RuntimeError("OCI_MODEL_DOWNLOAD_ENABLED=true requires the 'oci' Python package") from exc

    config_file = Path(os.getenv("OCI_CONFIG_FILE", "~/.oci/config")).expanduser()
    profile = os.getenv("OCI_CONFIG_PROFILE", "DEFAULT")
    config = oci.config.from_file(str(config_file), profile)
    client = oci.object_storage.ObjectStorageClient(config)

    for artifact in artifacts:
        object_name = os.getenv(artifact.env_var, artifact.default_object_name)
        if artifact.target.exists() and not force_download:
            state["artifacts"][artifact.key] = _artifact_state(artifact, "exists", object_name)
            continue

        artifact.target.parent.mkdir(parents=True, exist_ok=True)
        response = client.get_object(
            namespace_name=namespace,
            bucket_name=bucket_name,
            object_name=object_name,
        )
        with artifact.target.open("wb") as artifact_file:
            artifact_file.write(response.data.content)
        state["artifacts"][artifact.key] = _artifact_state(artifact, "downloaded", object_name)

    return state


def _artifact_state(artifact: ArtifactSpec, status: str, object_name: str | None = None) -> dict[str, Any]:
    return {
        "status": status,
        "path": str(artifact.target),
        "exists": artifact.target.exists(),
        "object_name": object_name,
    }


def _env_bool(name: str, *, default: bool) -> bool:
    raw_value = os.getenv(name)
    if raw_value is None:
        return default
    return raw_value.strip().lower() in {"1", "true", "yes", "y", "on"}
