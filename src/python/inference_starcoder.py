# inference_script.py
from transformers import AutoModelForCausalLM, AutoTokenizer
import sys
import json
import torch
import random
import warnings

from NoCUDAException import NoCUDAException

warnings.filterwarnings("ignore")

# Load the model and tokenizer
model_name = "bigcode/starcoder2-15b"
access_token = ""
device = "cpu"
if torch.cuda.is_available():
    device = "cuda:0"
else:
    raise NoCUDAException()

tokenizer = AutoTokenizer.from_pretrained(model_name, use_auth_token=access_token, padding_side='left')
model = AutoModelForCausalLM.from_pretrained(model_name, use_auth_token=access_token).to(device)


def generate_completion(prefix, suffix, mode):
    input_text = f"<fim_prefix>{prefix}\n<fim_suffix>{suffix}<fim_middle>"
    inputs = tokenizer(prefix, suffix, return_tensors='pt', truncation=True, max_length=1024).to(device)
    # inputs = tokenizer(input_text, return_tensors='pt', truncation=True, max_length=1024).to("cpu")

    max_new_toks = len(prefix)
    if mode == "multi-line":
        max_new_toks = len(prefix)
    elif mode == "single-line":
        max_new_toks = len(prefix) // prefix.count("\n")
    elif mode == "random-span":
        min_tokens = 5
        max_tokens = 500
        max_new_toks = random.randint(min_tokens, max_tokens)

    with torch.no_grad():
        outputs = model.generate(
            inputs['input_ids'],
            attention_mask=inputs['attention_mask'],
            max_new_tokens=max_new_toks,
            pad_token_id=tokenizer.eos_token_id
        )
    return tokenizer.decode(outputs[0], skip_special_tokens=True)


if __name__ == "__main__":
    try:
        input_data = json.load(sys.stdin)
        prefix = input_data["prefix"]
        suffix = input_data["suffix"]
        mode = input_data["selectedCodeGen"]

        completion = generate_completion(prefix, suffix, mode)
        result = {"completion": completion}
        print(json.dumps(result))
    except Exception as e:
        error_result = {"error": str(e)}
        print(json.dumps(error_result))

