import os
import joblib

from .oci_storage import download_model

MODELS_DIR = os.getenv("MODELS_LOCAL_DIR", "./models")

MODEL_FILES = {
    "scaler_categoria": "scaler_categoria.pkl",
    "label_encoder": "label_encoder_categoria.pkl",
    "random_forest_model": "random_forest_model (champion).pkl",
    "modelo_financial_stability": "modelo_financial_stability.pkl",
    "modelo_perfil_financiero": "modelo_perfil_financiero.pkl"  
}

models ={}
def load_models():

    os.makedirs(MODELS_DIR, exist_ok=True)

    for model_name, object_name in MODEL_FILES.items():

        local_path = os.path.join( MODELS_DIR, f"{model_name}.pkl" )

        # Descargar solamente si no existe localmente
        if not os.path.exists(local_path):

            print( f"Descargando {object_name}...")

            download_model(
                object_name,
                local_path
            )

        print(f"Cargando {model_name}..." )

        models[model_name] = joblib.load(local_path)

    print("Todos los modelos fueron cargados.")

def get_model(model_name) -> dict:

    if model_name not in models:
        raise ValueError(
            f"Modelo no encontrado: {model_name}"
        )

    return models[model_name]