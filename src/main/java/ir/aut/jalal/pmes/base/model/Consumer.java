package ir.barook.barookcore.base.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "consumers")
@NoArgsConstructor
@Data
public class Consumer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "type", nullable = false)
  private String type;
}