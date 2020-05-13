package org.chobit.ar4j.core.exception;


import org.chobit.ar4j.core.Model;

public class ModelUnRegisterException extends RuntimeException {

    public ModelUnRegisterException(Class<? extends Model> modelClass) {
        super(String.format("Model Class need to be registered first : %s", modelClass.getCanonicalName()));
    }


    public ModelUnRegisterException(Class<? extends Model> modelClass, Throwable cause) {
        super(String.format("Model Class need to be registered first : %s", modelClass.getCanonicalName()), cause);
    }

}
