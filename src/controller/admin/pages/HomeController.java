package controller.admin.pages;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import model.Datasource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * This class handles the admin home page.
 * @author      Sajmir Doko
 */
public class HomeController {

    @FXML
    public Label productsCount;
    @FXML
    public Label customersCount;

    //For the bar chart
    @FXML
    private BarChart<String, Number> revenueTrends; // Use specific types for better type safety.
    @FXML
    private NumberAxis y;
    @FXML
    private CategoryAxis x;

    @FXML
    private PieChart categoryPieChart; // Reference to the PieChart in the FXML file
//    @FXML
//    private GridPane topsellgrid;

    @FXML
    private Label orderCount;
    @FXML
    private Label revenueCount;
    @FXML
    private Label lowStockCount;



    /**
     * This method gets the products count for the admin dashboard and sets it to the productsCount label.
     * @since                   1.0.0
     */
    public void getDashboardProdCount() {
        Task<Integer> getDashProdCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllProducts();
            }
        };

        getDashProdCount.setOnSucceeded(e -> {
            productsCount.setText(String.valueOf(getDashProdCount.valueProperty().getValue()));

        });

        new Thread(getDashProdCount).start();
    }

    /**
     * This method gets the customers count for the admin dashboard and sets it to the customersCount label.
     * @since                   1.0.0
     */
    public void getDashboardCostCount() {
        Task<Integer> getDashCostCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllCustomers();
            }
        };

        getDashCostCount.setOnSucceeded(e -> {
            customersCount.setText(String.valueOf(getDashCostCount.valueProperty().getValue()));
        });

        new Thread(getDashCostCount).start();
    }

    // TODO
    //  Add best sellers
    //  Add latest sold products

    @FXML
    public void initializeTrendChart() {
        // Set axis labels
        x.setLabel("Days");
        y.setLabel("Revenue (in USD)");

        // Create a series for revenue data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Last 7 Days Revenue");

        // Fetch the last 7 days of revenue using the new method
        Map<String, Double> last7DaysRevenue = Datasource.getInstance().fetchLast7DaysRevenue();

        // Add data to the series
        for (Map.Entry<String, Double> entry : last7DaysRevenue.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Add series to the BarChart
        revenueTrends.getData().add(series);
    }
//    private Map<String, Double> fetchLast7DaysRevenue() {
//        // Simulate fetching revenue for the last 7 days
//        Map<String, Double> revenueData = new LinkedHashMap<>();
//
//        // Add dummy data (replace this with actual data from your database)
//        revenueData.put("Day 1", 1200.00);
//        revenueData.put("Day 2", 1500.00);
//        revenueData.put("Day 3", 1700.00);
//        revenueData.put("Day 4", 2000.00);
//        revenueData.put("Day 5", 1800.00);
//        revenueData.put("Day 6", 2100.00);
//        revenueData.put("Day 7", 2300.00);
//
//        return revenueData;
//    }

    //gridpane text setter for pie charts (not used)
    public void setLabelTexts(GridPane gridPane, ArrayList<String> percentages) {
        int index = 0; // To track the index of the ArrayList

        for (Node node : gridPane.getChildren()) {
            if (node instanceof Label && index < percentages.size()) {
                Label label = (Label) node;
                label.setText(percentages.get(index)); // Set the text from the ArrayList
                index++; // Move to the next item in the ArrayList
            }
        }
    }


    public void initializeTopCategoryPie() {
        Task<ObservableList<PieChart.Data>> fetchPieDataTask = new Task<>() {
            @Override
            protected ObservableList<PieChart.Data> call() {
                // Fetching data from the database
                List<PieChart.Data> dataList = Datasource.getInstance().fetchTopCategories();
                return FXCollections.observableArrayList(dataList);
            }
        };

        fetchPieDataTask.setOnSucceeded(event -> {
            ObservableList<PieChart.Data> pieData = fetchPieDataTask.getValue();

            // Bind category names to their values for display
            pieData.forEach(data ->
                    data.nameProperty().bind(
                            Bindings.concat(
                                    data.getName(), " - ", String.format("%.1f%%", data.getPieValue())
                            )
                    )
            );

            // Add data to the PieChart
            categoryPieChart.setLegendVisible(true);

            categoryPieChart.setData(pieData);
            categoryPieChart.setTitle("Best-Selling Categories");

            // Enable label visibility (CSS-related enhancement)
            categoryPieChart.setLabelsVisible(true);
            ArrayList<String> categoryPercentage = new ArrayList<>();
            for (PieChart.Data data : categoryPieChart.getData()) {
                // Get the name and value of each pie slice
                String name = data.getName();
                double value = data.getPieValue();

                // Get the color of each slice
                Node node = data.getNode(); // Get the Node representing the pie slice

                System.out.println("Category: " + name + ", Value: " + value );
                categoryPercentage.add(name);
            }
//            setLabelTexts(topsellgrid, categoryPercentage);

        });

        fetchPieDataTask.setOnFailed(event -> {
            System.out.println("Failed to fetch pie data: " + fetchPieDataTask.getException().getMessage());
        });
        new Thread(fetchPieDataTask).start();
    }


    /**
     * Gets the total orders and updates the orderCount label.
     */
    public void getDashboardOrderCount() {
        Task<Integer> getOrderCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countAllOrders();
            }
        };

        getOrderCount.setOnSucceeded(e -> {
            orderCount.setText(String.valueOf(getOrderCount.valueProperty().getValue()));
        });

        new Thread(getOrderCount).start();
    }

    /**
     * Gets the total revenue and updates the revenueCount label.
     */
    public void getDashboardRevenueCount() {
        Task<Double> getRevenueCount = new Task<Double>() {
            @Override
            protected Double call() {
                return Datasource.getInstance().getTotalRevenue();
            }
        };

        getRevenueCount.setOnSucceeded(e -> {
            revenueCount.setText(String.format("$%.2f", getRevenueCount.valueProperty().getValue()));
        });

        new Thread(getRevenueCount).start();
    }

    /**
     * Gets the count of low-stock products and updates the lowStockCount label.
     */
    public void getDashboardLowStockCount() {
        Task<Integer> getLowStockCount = new Task<Integer>() {
            @Override
            protected Integer call() {
                return Datasource.getInstance().countLowStockProducts();
            }
        };

        getLowStockCount.setOnSucceeded(e -> {
            lowStockCount.setText(String.valueOf(getLowStockCount.valueProperty().getValue()));
        });

        new Thread(getLowStockCount).start();
    }





}