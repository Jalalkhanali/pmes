package ir.barook.barookcore.base.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emissions")
@NoArgsConstructor
@Data
public class Emissions {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "energy_source_id", nullable = false)
  private EnergySource energySource;

  @Column(name = "ghg_emissions", nullable = false)
  private BigDecimal ghgEmissions;

  @Column(name = "date", nullable = false)
  private LocalDate date;
}