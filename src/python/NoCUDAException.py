class NoCUDAException(Exception):
    def __init__(self, message="Unfortunately, there is no CUDA installed on your device.\n"
                               "Please make sure you have one installed. If no, please use other model.\n"
                               "CUDA required for this model"):
        self.message = message
        super().__init__(self.message)
