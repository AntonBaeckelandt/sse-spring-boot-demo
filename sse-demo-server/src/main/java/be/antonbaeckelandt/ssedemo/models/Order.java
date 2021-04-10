package be.antonbaeckelandt.ssedemo.models;

public class Order {

    private int id;
    private String customerName;
    private String mealName;

    public Order(int id, String customerName, String mealName) {
        this.id = id;
        this.customerName = customerName;
        this.mealName = mealName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }
}
