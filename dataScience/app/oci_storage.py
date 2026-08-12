import os
from pathlib import Path
import oci
from dotenv import load_dotenv

load_dotenv()

# CONFIGURACIÓN OCI
OCI_CONFIG_FILE = os.getenv(
    "OCI_CONFIG_FILE",
    os.path.expanduser("~/.oci/config")
)

OCI_PROFILE = os.getenv("OCI_CONFIG_PROFILE", "DEFAULT")

OCI_NAMESPACE = os.getenv("OCI_NAMESPACE")
OCI_BUCKET_NAME = os.getenv("OCI_BUCKET_NAME")

config = oci.config.from_file(
    file_location=OCI_CONFIG_FILE,
    profile_name=OCI_PROFILE
)
object_storage = oci.object_storage.ObjectStorageClient(config)

# DESCARGAR OBJETO
def download_model(object_name: str,local_path: str):
    response = object_storage.get_object(
        namespace_name=OCI_NAMESPACE,
        bucket_name=OCI_BUCKET_NAME,
        object_name=object_name
    )

    Path(local_path).parent.mkdir(
        parents=True,
        exist_ok=True
    )

    with open(local_path, "wb") as file:
        file.write(response.data.content)

    return local_path

# LISTAR MODELOS
def list_models():

    response = object_storage.list_objects(
        namespace_name=OCI_NAMESPACE,
        bucket_name=OCI_BUCKET_NAME
    )

    return [
        obj.name
        for obj in response.data.objects
    ]