package it.eng.d4s.sa3.repository;


public class RepositoryException extends Exception{

   private static final long serialVersionUID = 1L;

   public RepositoryException(String message) {
       super(message);
   }
   
   public RepositoryException(Throwable e) {
       super(e);
   }
}
