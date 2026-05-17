package co.edu.corhuila.inventory_service.Dto;

public class InventoryAuditStatusRequest {
    private String status;
    private String observation;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}
