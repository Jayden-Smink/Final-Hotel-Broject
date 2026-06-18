# 🏨 Hotel Simulatie

Een Java-simulatieapplicatie die het dagelijkse leven in een hotel visueel simuleert. Gasten komen aan, checken in bij de receptie, bezoeken faciliteiten, gebruiken de lift of trap, en worden gevolgd door schoonmakers die kamers opruimen na vertrek. Als extra scenario's zijn er een brandalarm-evacuatie en een Godzilla-aanval beschikbaar.

---

## Teamleden

| Naam | Studentnummer |
|---|---|
| Aryan Panchoe | 25068962 |
| Benjamin Mrkajl | 25158791 |
| Erim Koçak | 25097385 |
| Jayden Smink | 25162780 |
| John Shoghnani | 24073806 |
| Raşit Karagöz | 25102249 |

---

## Inhoudsopgave

1. [Vereisten](#vereisten)
2. [Opstarten](#opstarten)
3. [Projectstructuur](#projectstructuur)
4. [Functionaliteiten](#functionaliteiten)
5. [Hotellay-outs](#hotellay-outs)
6. [Instellingen](#instellingen)
7. [Tests uitvoeren](#tests-uitvoeren)
8. [Design patterns](#design-patterns)
9. [Bekende beperkingen](#bekende-beperkingen)

---

## Vereisten

- **Java 17** of hoger
- **IntelliJ IDEA** (aanbevolen) of een andere Java IDE
- De meegeleverde library `lib/HotelEventsObs.jar` (observer-patroon voor hotel-events)

### Externe libraries (al meegeleverd in het project)

| Library | Versie | Gebruik |
|---|---|---|
| `HotelEventsObs.jar` | — | Hotel-event observerpatroon |
| `google-code-gson` | — | JSON-parsing van lay-outbestanden |
| `org.json` | 20251224 | Aanvullende JSON-verwerking |
| `mockito-core` | 5.5.0 | Mocking in tests |
| `assertj-core` | 3.27.7 | Assertions in tests |
| `junit-jupiter` | — | Testframework (JUnit 5) |

---

## Opstarten

### Via IntelliJ IDEA

1. Open het project: **File → Open** → selecteer de map `Final-Hotel-Broject`
2. Zorg dat de SDK is ingesteld op **Java 17+** via **File → Project Structure → SDK**
3. Controleer of `lib/HotelEventsObs.jar` is toegevoegd als library via **File → Project Structure → Libraries**
4. Zoek `src/main.java` op in de projectboom
5. Klik op de groene ▶ knop naast `public static void main` om de simulatie te starten

### Wat je ziet bij het opstarten

Bij het starten verschijnt een **startscherm** met twee opties:

- **Nieuw spel starten** — opent een instellingendialoog waar je het hotel configureert
- **Lay-out laden** — laad een bestaand `.json`-lay-outbestand uit de `src/layouts/` map

---

## Projectstructuur

```
src/
├── main.java                        # Ingangspunt van de applicatie
│
├── controller/                      # Game-logica en controllers
│   ├── SimulationController.java    # Centrale controller, koppelt alle delen
│   ├── SimulationPanel.java         # Hoofd-UI-panel (lay-out + gameloop)
│   ├── GameLoop.java                # ~60fps render/update-loop (Swing Timer)
│   ├── HotelTimeEngine.java         # Pauzeren en snelheid instellen
│   ├── GuestController.java         # Inchecken en updaten van gasten
│   ├── GuestMover.java              # Bewegingslogica per tick
│   ├── GuestNavigator.java          # Bewegingsdoelen instellen (kamer/faciliteit/exit)
│   ├── GuestLocationHandler.java    # Reactie op aankomst (AT_DESTINATION)
│   ├── GuestActivityController.java # Activiteitstimer en faciliteiten-rotatie
│   ├── GuestSpawner.java            # Gasten aanmaken en spawnen
│   ├── GuestCreator.java            # Guest-objecten aanmaken
│   ├── GuestCheckInValidator.java   # Validatie van check-in verzoeken
│   ├── ElevatorController.java      # Lift-logica (in-/uitstappen, wachttimer)
│   ├── CleanerController.java       # State machine voor schoonmakers
│   ├── CleanerAssigner.java         # Toewijzen van kamers aan schoonmakers
│   ├── CleanerMover.java            # Beweging van schoonmakers
│   ├── CleanerPool.java             # Aanmaken van schoonmakers
│   ├── CleanerStateHandler.java     # State-overgangen voor schoonmakers
│   ├── EmergencyHandler.java        # Brandalarm/evacuatie-logica
│   ├── GodzillaController.java      # Godzilla-aanvalslogica
│   ├── ReceptionistController.java  # Kamer toewijzen, gast naar receptie/kamer sturen
│   ├── RoomController.java          # Kamer reserveren, vrijgeven, faciliteiten zoeken
│   ├── RouteCalculator.java         # Reistijden lift vs trap vergelijken
│   ├── layoutGenerator.java         # JSON → Area-objecten genereren
│   ├── LayoutFileReader.java        # JSON-bestand inlezen
│   ├── LayoutParser.java            # JSON-inhoud parsen naar Area-objecten
│   ├── LayoutFlipper.java           # Lay-out horizontaal spiegelen
│   └── StartScreenController.java   # Logica voor het startscherm
│
├── model/                           # Domeinobjecten (pure data/logica)
│   ├── SimulationData.java          # Centrale state-container (gedeeld door alle controllers)
│   ├── Guest.java                   # Gastobject met state, positie, activiteit
│   ├── GuestState.java              # Enum: IDLE, WALKING, IN_LIFT, IN_QUEUE, ...
│   ├── Cleaner.java                 # Schoonmaker met state, timer, wachtrij
│   ├── CleanerState.java            # Enum: IDLE, WALKING_TO_ROOM, CLEANING, WALKING_BACK
│   ├── Elevator.java                # Liftobject met positie, passagiers, capaciteit
│   ├── Area.java                    # Hotelruimte (type, positie, capaciteit, bezetting)
│   ├── RoomType.java                # Enum: ROOM, CINEMA, FITNESS, RESTAURANT, LOBBY, ...
│   ├── Person.java                  # Basisklasse voor Guest en Cleaner
│   ├── PersonType.java              # Enum: GUEST, CLEANER
│   ├── Receptionist.java            # Kamer toewijzen en gasten uitchecken
│   ├── RuimteService.java           # Statische service: ruimtes reserveren/vrijgeven
│   ├── StairModel.java              # Trap-positie en reistijdberekening
│   ├── GodzillaModel.java           # State van de Godzilla-aanval
│   ├── IDestructionStrategy.java    # Interface voor vernietigingsstrategieën
│   ├── FireDestruction.java         # Brandstrategie (graduale vernieling)
│   ├── InstantDestruction.java      # Directe vernielingsstrategie
│   ├── CleanerSettings.java         # Schoonmaakduur in frames
│   ├── FacilitySettings.java        # Verblijfsduur per faciliteit in frames
│   └── GuestSettings.java           # Lift-wachttimer timeout
│
├── view/                            # UI-componenten en renderers
│   ├── StartScreen.java             # Startscherm (JFrame)
│   ├── StartScreenComponents.java   # UI-componenten voor het startscherm
│   ├── SettingsDialog.java          # Instellingendialoog
│   ├── SimulationRenderer.java      # Hoofdrenderer (delegeert naar sub-renderers)
│   ├── AreaRenderer.java            # Tekent hotelruimtes
│   ├── GuestRenderer.java           # Tekent gasten
│   ├── CleanerRenderer.java         # Tekent schoonmakers
│   ├── ElevatorRenderer.java        # Tekent de lift
│   ├── GodzillaRenderer.java        # Tekent Godzilla
│   ├── AssetLoader.java             # Laadt afbeeldingen (PNG's) uit de Picture-map
│   ├── LogPanel.java                # Zijpaneel met realtime logberichten
│   ├── TimeControlPanel.java        # Onderste balk: pauze, snelheid, stopwatch
│   ├── StopwatchDisplay.java        # Weergave van de simulatietijd
│   ├── StopwatchTimer.java          # Stopwatch-logica
│   ├── RoomOverviewPanel.java       # Kameroverzicht (opent bij klik op lobby)
│   ├── SoundManager.java            # Achtergrondmuziek en geluidseffecten
│   └── Picture/                     # Alle PNG-sprites (normaal/broken/burned)
│
├── factory/                         # Objectfabrieken
│   ├── PersonFactory.java           # Maakt Guest- of Cleaner-objecten
│   ├── RoomFactory.java             # Maakt Area-objecten op basis van RoomType
│   └── UIFactory.java               # Maakt gestijlde Swing-knoppen
│
├── layouts/                         # Hotellay-outs als JSON-bestanden
│   ├── layout.json                  # Standaard lay-out
│   ├── hotel_layout_uitgebreid_correct.json
│   ├── hotel_layout_uitgebreid_breder.json
│   ├── hotel_layout_breder_met_torens.json
│   └── hotel_layout_breder_10_extra_verdiepingen_restaurants.json
│
└── Music/                           # Geluidsbestanden
    ├── music.wav                    # Normale achtergrondmuziek
    ├── evacuate.wav                 # Muziek tijdens brandalarm
    └── godzilla.wav                 # Muziek tijdens Godzilla-aanval

tests/                               # JUnit 5-testbestanden (spiegelt src/ structuur)
├── controller/                      # Tests voor alle controllers
├── model/                           # Tests voor alle modelklassen
├── view/                            # Tests voor view-componenten
└── factory/                         # Tests voor de fabrieken
```

---

## Functionaliteiten

### Normale simulatie

- **Gasten** arriveren automatisch, lopen naar de receptie, krijgen een kamer toegewezen en beginnen activiteiten (naar restaurant, cinema of fitness gaan, terugkeren naar kamer, uitchecken)
- **Schoonmakers** worden na het vertrek van een gast automatisch naar de kamer gestuurd; ze hebben een wachtrij als er meerdere kamers tegelijk vuil worden
- **Lift** transporteert gasten tussen verdiepingen; gasten wachten in een rij en worden verwijderd als ze te lang wachten (timeout)
- **Trap** is een alternatief voor de lift; `RouteCalculator` vergelijkt reistijden en kiest de snelste route
- **Klik op de lobby** om een kameroverzicht te openen met de actuele bezettingsstatus

### Tijdsbediening (onderste balk)

| Knop | Functie |
|---|---|
| ▶ / ⏸ | Simulatie starten of pauzeren |
| 1× / 2× / 4× | Simulatiesnelheid aanpassen |
| Stopwatch | Toont de verstreken simulatietijd |

### Speciale scenario's

| Knop | Wat er gebeurt |
|---|---|
| 🔥 Brandalarm | Alle gasten worden geëvacueerd naar de uitgang; muziek wisselt naar evacuatiemuziek |
| 🦖 Test Godzilla | Godzilla verschijnt en vernietigt het hotel kolom voor kolom; muziek wisselt; simulatie stopt na afloop met een overlay |

---

## Hotellay-outs

Het hotel wordt geladen vanuit een JSON-bestand in `src/layouts/`. Je kunt op het startscherm een bestaande lay-out kiezen of zelf een JSON-bestand aanmaken.

### JSON-formaat

```json
[
  {
    "AreaType": "Room",
    "Classification": "3 Star",
    "Position": "2, 1",
    "Dimension": "1, 1"
  },
  {
    "AreaType": "Cinema",
    "Position": "1, 3",
    "Dimension": "2, 2"
  },
  {
    "AreaType": "Restaurant",
    "Capacity": 5,
    "Position": "5, 4",
    "Dimension": "2, 1"
  }
]
```

### Velden

| Veld | Verplicht | Beschrijving |
|---|---|---|
| `AreaType` | Ja | Type ruimte: `Room`, `Cinema`, `Restaurant`, `Fitness` |
| `Position` | Ja | Kolom en rij op het hotelraster (`"kolom, rij"`) |
| `Dimension` | Ja | Breedte en hoogte in tiles (`"breedte, hoogte"`) |
| `Capacity` | Nee | Max. aantal gasten tegelijk (standaard: Room=1, Cinema=10, Restaurant=5) |
| `Classification` | Nee | Sterrenclassificatie van een kamer (`"1 Star"` t/m `"5 Star"`) |

De infrastructuur (lift, trap, lobby, receptie) wordt automatisch door `layoutGenerator` toegevoegd — die hoef je niet in het JSON-bestand op te nemen.

---

## Instellingen

Bij het starten van een nieuw spel verschijnt een dialoogvenster met de volgende opties:

| Instelling | Standaard | Beschrijving |
|---|---|---|
| Kamer capaciteit | 1 | Max. gasten per kamer |
| Schoonmaakduur (sec) | 30 | Hoe lang een schoonmaker over een kamer doet |
| Aantal schoonmakers | 2 | Hoeveel schoonmakers actief zijn |
| Scenario | 1 | Keuze uit beschikbare simulatiescenario's |
| Cinema duur (sec) | 30 | Hoe lang een gast in de cinema verblijft |
| Restaurant duur (sec) | 10 | Hoe lang een gast in het restaurant verblijft |
| Fitness duur (sec) | 15 | Hoe lang een gast in de fitness verblijft |
| Lift wachttijd (sec) | 60 | Na hoeveel seconden wachten een gast de lift opgeeft en vertrekt |

---

## Tests uitvoeren

De tests staan in de `tests/` map en zijn geschreven met **JUnit 5**. Ze zijn te vinden per package (`controller`, `model`, `view`, `factory`).

### Via IntelliJ IDEA

1. Klik rechts op de `tests/` map in de projectboom
2. Kies **Run 'All Tests'**

Of klik rechts op een specifiek testbestand en kies **Run**.

### Testdekking

| Package | Aantal testklassen | Wat wordt getest |
|---|---|---|
| `controller` | 26 | Alle controllers incl. state machines, timers, routing |
| `model` | 13 | Domeinobjecten, enums, services, vernietigingsstrategieën |
| `view` | 6 | Renderers, UI-panels, headless Swing-rendering |
| `factory` | 3 | PersonFactory, RoomFactory, UIFactory |

> **Let op:** `StartScreenTest` vereist een grafisch scherm en wordt automatisch overgeslagen in headless omgevingen (CI). `SimulationController.notify(HotelEvent)` is niet getest omdat de `hotelevents`-package alleen als `.class` beschikbaar is.

---

## Design patterns

Het project maakt gebruik van de volgende design patterns:

| Pattern | Waar toegepast |
|---|---|
| **MVC** | Strikte scheiding tussen `model/`, `view/` en `controller/` |
| **Factory** | `PersonFactory`, `RoomFactory`, `UIFactory` — objectcreatie gecentraliseerd |
| **Strategy** | `IDestructionStrategy` met `FireDestruction` en `InstantDestruction` voor Godzilla |
| **Observer** | `HotelEventsObs.jar` voor hotel-events |
| **SRP** | Elke controller heeft één verantwoordelijkheid (bijv. `GuestMover` beweegt, `GuestNavigator` bepaalt het doel) |

---

## Bekende beperkingen

- De `hotelevents`-package is alleen als gecompileerde `.class` beschikbaar; de broncode ontbreekt
- `StartScreenTest` wordt overgeslagen zonder grafisch scherm
- Erim Koçak heeft bijgedragen aan het project maar staat op sommige Trello-kaarten niet als formele assignee vermeld
