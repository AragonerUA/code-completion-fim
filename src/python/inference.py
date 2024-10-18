# inference_script.py
from transformers import AutoModelForCausalLM, AutoTokenizer
import sys
import json
import torch
import random
import warnings
warnings.filterwarnings("ignore")
from tqdm.auto import tqdm

model_name = "bigcode/tiny_starcoder_py"
access_token = "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"
tokenizer = AutoTokenizer.from_pretrained(model_name, use_auth_token=access_token, padding_side='left')
model = AutoModelForCausalLM.from_pretrained(model_name, use_auth_token=access_token).to("cpu")


# def generate_completion(prefix, middle, suffix):
def generate_completion(prefix, suffix, mode):
    input_text = f"<fim_prefix>{prefix}\n<fim_suffix>{suffix}<fim_middle>"
    # input_text = f"<fim_prefix>{prefix}\n<fim_middle>{middle}\n<fim_suffix>{suffix}"
    inputs = tokenizer(input_text, return_tensors='pt', truncation=True, max_length=1024).to("cpu")
    # inputs = tokenizer.encode(input_text, return_tensors='pt', truncation=True, max_length=1024).to("cpu")

    # inputs = tokenizer(input_text, return_tensors='pt')
    # pad_token_id = tokenizer.pad_token_id if tokenizer.pad_token_id is not None else tokenizer.eos_token_id

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
            pad_token_id=tokenizer.eos_token_id,
            no_repeat_ngram_size=5
        )
    generated_whole_text = tokenizer.decode(outputs[0], skip_special_tokens=True)
    suffix_start_index = generated_whole_text.find(suffix)

    if suffix_start_index == -1:
        raise ValueError("Suffix not found in the generated output.")

    suffix_end_index = suffix_start_index + len(suffix)
    return generated_whole_text[suffix_end_index:]


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
