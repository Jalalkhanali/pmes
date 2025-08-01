package ir.barook.barookcore.base.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "energy_sources")
@NoArgsConstructor
@Data
public class EnergySource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "type")
  private String type;

  @Column(name = "description")
  private String description;
}