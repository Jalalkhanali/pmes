package ir.barook.barookcore.base.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "energy_consumption")
@NoArgsConstructor
@Data
public class EnergyConsumption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "consumer_id", nullable = false)
  private Consumer consumer;

  @ManyToOne
  @JoinColumn(name = "energy_source_id", nullable = false)
  private EnergySource energySource;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "date", nullable = false)
  private LocalDate date;
}