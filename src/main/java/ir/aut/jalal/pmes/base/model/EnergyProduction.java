package ir.barook.barookcore.base.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "energy_production")
@NoArgsConstructor
@Data
public class EnergyProduction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "energy_source_id", nullable = false)
  private EnergySource energySource;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "date", nullable = false)
  private LocalDate date;
}