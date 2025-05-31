import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Optional;

public class AirlineReservationFX extends Application {

    static class Flight {
        int flightNo;
        String source;
        String destination;
        int seatsAvailable;

        Flight(int flightNo, String source, String destination, int seatsAvailable) {
            this.flightNo = flightNo;
            this.source = source;
            this.destination = destination;
            this.seatsAvailable = seatsAvailable;
        }

        @Override
        public String toString() {
            return flightNo + " : " + source + " -> " + destination + " (Seats: " + seatsAvailable + ")";
        }
    }

    static class Passenger {
        int bookingId;
        int flightNo;
        String name;

        Passenger(int bookingId, int flightNo, String name) {
            this.bookingId = bookingId;
            this.flightNo = flightNo;
            this.name = name;
        }
    }

    private final ObservableList<Flight> flights = FXCollections.observableArrayList();
    private final ObservableList<Passenger> passengers = FXCollections.observableArrayList();

    private int bookingCount = 0;

    @Override
    public void start(Stage primaryStage) {
        initializeFlights();

        primaryStage.setTitle("Airline Ticket Reservation System");

        Button btnReserve = new Button("1: Reservation");
        Button btnCancel = new Button("2: Cancellation");
        Button btnRecords = new Button("3: Passenger Records");
        Button btnEnquiry = new Button("4: Enquiry");
        Button btnList = new Button("5: List of Passengers");
        Button btnQuit = new Button("6: Quit");

        btnReserve.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnRecords.setMaxWidth(Double.MAX_VALUE);
        btnEnquiry.setMaxWidth(Double.MAX_VALUE);
        btnList.setMaxWidth(Double.MAX_VALUE);
        btnQuit.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(10, btnReserve, btnCancel, btnRecords, btnEnquiry, btnList, btnQuit);
        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(300);

        btnReserve.setOnAction(e -> reservation());
        btnCancel.setOnAction(e -> cancellation());
        btnRecords.setOnAction(e -> passengerRecords());
        btnEnquiry.setOnAction(e -> enquiry());
        btnList.setOnAction(e -> listPassengers());
        btnQuit.setOnAction(e -> primaryStage.close());

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeFlights() {
        flights.addAll(
                new Flight(101, "New York", "London", 5),
                new Flight(102, "Paris", "Tokyo", 3),
                new Flight(103, "Delhi", "Dubai", 4),
                new Flight(104, "Sydney", "Singapore", 2),
                new Flight(105, "Mumbai", "New York", 6)
        );
    }

    private Flight findFlight(int flightNo) {
        for (Flight f : flights) {
            if (f.flightNo == flightNo) return f;
        }
        return null;
    }

    private Passenger findPassengerByBookingId(int bookingId) {
        for (Passenger p : passengers) {
            if (p.bookingId == bookingId) return p;
        }
        return null;
    }

    private ObservableList<Passenger> findPassengersByName(String name) {
        ObservableList<Passenger> result = FXCollections.observableArrayList();
        for (Passenger p : passengers) {
            if (p.name.equalsIgnoreCase(name)) {
                result.add(p);
            }
        }
        return result;
    }

    private void reservation() {
        // Flight selection dialog
        ChoiceDialog<Flight> flightChoice = new ChoiceDialog<>(flights.get(0), flights);
        flightChoice.setTitle("Reservation");
        flightChoice.setHeaderText("Select Flight to Book");
        flightChoice.setContentText("Available Flights:");

        Optional<Flight> flightResult = flightChoice.showAndWait();
        if (!flightResult.isPresent()) return;

        Flight selectedFlight = flightResult.get();

        if (selectedFlight.seatsAvailable <= 0) {
            showAlert(Alert.AlertType.ERROR, "Reservation Failed", "No seats available on this flight.");
            return;
        }

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Reservation");
        nameDialog.setHeaderText("Enter Passenger Name");
        nameDialog.setContentText("Name:");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (!nameResult.isPresent() || nameResult.get().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Passenger name cannot be empty.");
            return;
        }

        bookingCount++;
        passengers.add(new Passenger(bookingCount, selectedFlight.flightNo, nameResult.get().trim()));
        selectedFlight.seatsAvailable--;

        showAlert(Alert.AlertType.INFORMATION, "Reservation Successful", "Booking ID: " + bookingCount);
    }

    private void cancellation() {
        TextInputDialog bookingDialog = new TextInputDialog();
        bookingDialog.setTitle("Cancellation");
        bookingDialog.setHeaderText("Enter Booking ID to Cancel");
        bookingDialog.setContentText("Booking ID:");

        Optional<String> result = bookingDialog.showAndWait();
        if (!result.isPresent()) return;

        int bookingId;
        try {
            bookingId = Integer.parseInt(result.get().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Booking ID must be a number.");
            return;
        }

        Passenger passenger = findPassengerByBookingId(bookingId);
        if (passenger == null) {
            showAlert(Alert.AlertType.ERROR, "Not Found", "Booking ID not found.");
            return;
        }

        Flight flight = findFlight(passenger.flightNo);
        if (flight != null) {
            flight.seatsAvailable++;
        }

        passengers.remove(passenger);
        showAlert(Alert.AlertType.INFORMATION, "Cancellation Successful", "Booking ID " + bookingId + " cancelled.");
    }

    private void passengerRecords() {
        ChoiceDialog<String> searchChoice = new ChoiceDialog<>("By Booking ID", "By Booking ID", "By Name");
        searchChoice.setTitle("Passenger Records");
        searchChoice.setHeaderText("Search Passenger Records");
        searchChoice.setContentText("Search by:");

        Optional<String> searchResult = searchChoice.showAndWait();
        if (!searchResult.isPresent()) return;

        if (searchResult.get().equals("By Booking ID")) {
            TextInputDialog bookingDialog = new TextInputDialog();
            bookingDialog.setTitle("Passenger Records");
            bookingDialog.setHeaderText("Enter Booking ID");
            bookingDialog.setContentText("Booking ID:");

            Optional<String> idResult = bookingDialog.showAndWait();
            if (!idResult.isPresent()) return;

            int bookingId;
            try {
                bookingId = Integer.parseInt(idResult.get().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Booking ID must be a number.");
                return;
            }

            Passenger p = findPassengerByBookingId(bookingId);
            if (p == null) {
                showAlert(Alert.AlertType.INFORMATION, "Not Found", "No record found for Booking ID: " + bookingId);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Passenger Found",
                        "Booking ID: " + p.bookingId + "\nName: " + p.name + "\nFlight No: " + p.flightNo);
            }

        } else {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Passenger Records");
            nameDialog.setHeaderText("Enter Passenger Name");
            nameDialog.setContentText("Name:");

            Optional<String> nameResult = nameDialog.showAndWait();
            if (!nameResult.isPresent()) return;

            ObservableList<Passenger> foundPassengers = findPassengersByName(nameResult.get().trim());
            if (foundPassengers.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Not Found", "No records found for name: " + nameResult.get());
            } else {
                StringBuilder sb = new StringBuilder();
                for (Passenger p : foundPassengers) {
                    sb.append("Booking ID: ").append(p.bookingId)
                      .append(", Flight No: ").append(p.flightNo).append("\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "Passengers Found", sb.toString());
            }
        }
    }

    private void enquiry() {
        TextInputDialog flightDialog = new TextInputDialog();
        flightDialog.setTitle("Flight Enquiry");
        flightDialog.setHeaderText("Enter Flight Number");
        flightDialog.setContentText("Flight No:");

        Optional<String> result = flightDialog.showAndWait();
        if (!result.isPresent()) return;

        int flightNo;
        try {
            flightNo = Integer.parseInt(result.get().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Flight number must be numeric.");
            return;
        }

        Flight flight = findFlight(flightNo);
        if (flight == null) {
            showAlert(Alert.AlertType.INFORMATION, "Not Found", "Flight number not found.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Flight Details",
                "Flight No: " + flight.flightNo + "\nSource: " + flight.source +
                        "\nDestination: " + flight.destination + "\nSeats Available: " + flight.seatsAvailable);
    }

    private void listPassengers() {
        if (passengers.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Passengers", "No passengers booked yet.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("List of Passengers");

        TableView<Passenger> table = new TableView<>();

        TableColumn<Passenger, Integer> colBookingId = new TableColumn<>("Booking ID");
        colBookingId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().bookingId).asObject());

        TableColumn<Passenger, Integer> colFlightNo = new TableColumn<>("Flight No");
        colFlightNo.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().flightNo).asObject());

        TableColumn<Passenger, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().name));

        table.getColumns().addAll(colBookingId, colFlightNo, colName);
        table.setItems(passengers);

        VBox vbox = new VBox(table);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 400, 300);

        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
