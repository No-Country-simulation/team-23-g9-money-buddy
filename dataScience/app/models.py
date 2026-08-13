import os
import joblib

from .oci_storage import download_model

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODELS_DIR = os.path.join(
    BASE_DIR,
    "models"
)

MODEL_FILES = {
    "scaler_categoria": "financial-modelsscaler_categoria.pkl",
    "label_encoder": "financial-modelslabel_encoder_categoria.pkl",
    "random_forest_model": "financial-modelsrandom_forest_model (champion).pkl",
    "modelo_financial_stability": "financial-modelsmodelo_financial_stability.pkl",
    "modelo_perfil_financiero": "financial-modelsmodelo_perfil_financiero.pkl"  
}

models ={}
def load_models():

    os.makedirs(MODELS_DIR, exist_ok=True)

    for model_name, object_name in MODEL_FILES.items():

        local_path = os.path.join(
            MODELS_DIR,
            f"{model_name}.pkl"
        )

        print(f"\n Modelo: {model_name}")
        print(f" Ruta local: {local_path}")

        if not os.path.exists(local_path):


            download_model(
                object_name,
                local_path
            )

            print(f"Descarga terminada: {local_path}")

        else:

            print(f"Ya existe localmente: {local_path}")

        print(f"Cargando {model_name}...")

        models[model_name] = joblib.load(local_path)

    print("\n Todos los modelos fueron cargados.")

def get_model(model_name) -> dict:

    if model_name not in models:
        raise ValueError(
            f"Modelo no encontrado: {model_name}"
        )

    return models[model_name]