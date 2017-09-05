package algorithm;

public abstract class AbstractConfidenceStrategy implements ConfidenceStrategy  {
    private Order order;
    public AbstractConfidenceStrategy(Order order){
        this.order = order;
    }

    public Order getOrder(){
        return order;
    }

}
