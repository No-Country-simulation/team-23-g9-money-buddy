from app.oci_artifacts import ArtifactSpec, download_artifacts_if_enabled


def test_oci_disabled_does_not_require_sdk_or_config(monkeypatch, tmp_path):
    monkeypatch.delenv("OCI_MODEL_DOWNLOAD_ENABLED", raising=False)
    monkeypatch.delenv("OCI_NAMESPACE", raising=False)
    monkeypatch.delenv("OCI_BUCKET_NAME", raising=False)

    artifact = ArtifactSpec(
        key="test_artifact",
        target=tmp_path / "missing.pkl",
        default_object_name="object.pkl",
        env_var="OCI_TEST_OBJECT",
    )

    state = download_artifacts_if_enabled(artifacts=(artifact,))

    assert state["enabled"] is False
    assert state["artifacts"]["test_artifact"]["status"] == "disabled"
    assert state["artifacts"]["test_artifact"]["exists"] is False


def test_oci_enabled_requires_namespace_and_bucket(monkeypatch, tmp_path):
    monkeypatch.setenv("OCI_MODEL_DOWNLOAD_ENABLED", "true")
    monkeypatch.delenv("OCI_NAMESPACE", raising=False)
    monkeypatch.delenv("OCI_BUCKET_NAME", raising=False)

    artifact = ArtifactSpec(
        key="test_artifact",
        target=tmp_path / "missing.pkl",
        default_object_name="object.pkl",
        env_var="OCI_TEST_OBJECT",
    )

    try:
        download_artifacts_if_enabled(artifacts=(artifact,))
    except RuntimeError as exc:
        assert "OCI_NAMESPACE and OCI_BUCKET_NAME" in str(exc)
    else:
        raise AssertionError("Expected missing OCI config to fail")
