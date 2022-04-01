package site.minnan.stock.infrastructure.exception;

/**
 * 数据统计中的异常
 *
 * @author Minnan on 2022/04/01
 */
public class ProcessingException extends Exception {

    public ProcessingException(){
        super();
    }

    public ProcessingException(String message){
        super(message);
    }

}
