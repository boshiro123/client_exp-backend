package back.client_exp_backend.exception;

public class ResourceAlreadyExistsException extends RuntimeException {

  public ResourceAlreadyExistsException(String message) {
    super(message);
  }

  public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
    super(String.format("%s уже существует с %s: '%s'", resourceName, fieldName, fieldValue));
  }
}