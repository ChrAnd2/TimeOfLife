# Life span estimation

The app estimates a life span in years. The user can replace it with their own value at any time
(`LifespanSetting.Manual`). The estimate is a rough orientation, not a medical prognosis, and the
app should say so wherever it shows the number.

Implementation: `sharedLogic/.../domain/usecase/StudyBasedLifespanEstimator.kt` adds up the
`yearsEffect` of each `HealthFactor`. Every factor and its numbers live in their own file in
`domain/model/lifespan/factor/`. The baseline table is
`data/datasource/StaticBaselineLifeExpectancyProvider.kt`.

## Method

```
estimate = baseline(country, sex) + Σ adjustment(factor)
```

The estimate is not clamped. Users who are already older than it are shown how long they have lived
beyond it.

Each factor's adjustment is the change in life expectancy a large cohort study reports for that
factor, compared with the study's reference group. Factors the user didn't provide add nothing.

## Baseline: life expectancy at birth

| Region            | Male | Female | Source                                                                                                                                                                                         |
|-------------------|-----:|-------:|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Germany           | 78.9 |   83.5 | Destatis, Sterbetafel 2024 ([Destatis: Sterbefälle und Lebenserwartung](https://www.destatis.de/DE/Themen/Gesellschaft-Umwelt/Bevoelkerung/Sterbefaelle-Lebenserwartung/_inhalt.html))         |
| Global (fallback) | 70.8 |   75.9 | WHO Global Health Estimates, 2019 ([WHO GHO: life expectancy](https://www.who.int/data/gho/data/themes/mortality-and-global-health-estimates/ghe-life-expectancy-and-healthy-life-expectancy)) |

For `Sex.DIVERSE` there is no separate statistic, so the mean of the male and female values is used.

## Adjustments

### Smoking

Doll R, Peto R, Boreham J, Sutherland I. *Mortality in relation to smoking: 50 years' observations
on male British doctors.* BMJ 2004;328:
1519. [doi:10.1136/bmj.38142.554479.AE](https://doi.org/10.1136/bmj.38142.554479.AE)

Continuing smokers died about 10 years earlier than lifelong non-smokers. Quitting at age 60, 50, 40
or 30 regained about 3, 6, 9 or 10 years.

| Status                   | Years |
|--------------------------|------:|
| Never                    |     0 |
| Current                  |   −10 |
| Former, quit before 35   |     0 |
| Former, quit 35–44       |    −1 |
| Former, quit 45–54       |    −4 |
| Former, quit 55 or later |    −7 |

### Alcohol

Wood AM et al. *Risk thresholds for alcohol consumption: combined analysis of individual-participant
data for 599 912 current drinkers in 83 prospective studies.* Lancet 2018;391:
1513–23. [doi:10.1016/S0140-6736 (18)30134-X](https://doi.org/10.1016/S0140-6736(18)30134-X)

Compared with drinking less than 100 g of alcohol per week, life expectancy at age 40 was about 6
months, 1–2 years and 4–5 years lower for 100–200, 200–350 and more than 350 g/week.

| Consumption         | Years |
|---------------------|------:|
| None / < 100 g/week |     0 |
| 100–200 g/week      |  −0.5 |
| 200–350 g/week      |  −1.5 |
| > 350 g/week        |  −4.5 |

### Physical activity

Moore SC et al. *Leisure Time Physical Activity of Moderate to Vigorous Intensity and Mortality: A
Large Pooled Cohort Analysis.* PLoS Med 2012;9 (11):
e1001335. [doi:10.1371/journal.pmed.1001335](https://doi.org/10.1371/journal.pmed.1001335)
([NIH summary](https://www.nih.gov/news-events/news-releases/nih-study-finds-leisure-time-physical-activity-extends-life-expectancy-much-45-years))

Compared with no leisure time activity: up to 75 min/week of brisk walking +1.8 years, meeting the
recommended level +3.4 years, 450+ min/week +4.5 years.

| Level                               | Years |
|-------------------------------------|------:|
| None                                |     0 |
| Low (< 150 min/week)                |  +1.8 |
| Meets guidelines (150–450 min/week) |  +3.4 |
| High (> 450 min/week)               |  +4.5 |

### Body mass index

Prospective Studies Collaboration. *Body-mass index and cause-specific mortality in 900 000 adults:
collaborative analyses of 57 prospective studies.* Lancet 2009;373:
1083–96. [doi:10.1016/S0140-6736 (09)60318-4](https://doi.org/10.1016/S0140-6736(09)60318-4)
([PMC full text](https://pmc.ncbi.nlm.nih.gov/articles/PMC2662372/))

Mortality was lowest at a BMI of about 22.5–25. At 30–35 kg/m² median survival was 2–4 years
shorter, at 40–45 kg/m² 8–10 years shorter.

| BMI     | Years | Note                                            |
|---------|------:|-------------------------------------------------|
| < 18.5  |    −1 | placeholder, the study gives no figure in years |
| 18.5–25 |     0 |                                                 |
| 25–30   |    −1 | interpolated                                    |
| 30–35   |    −3 | study: 2–4                                      |
| 35–40   |    −6 | interpolated                                    |
| ≥ 40    |    −9 | study: 8–10                                     |

### Health conditions (diabetes, heart attack, stroke)

The Emerging Risk Factors Collaboration. *Association of Cardiometabolic Multimorbidity With
Mortality.* JAMA 2015;314 (1):
52–60. [doi:10.1001/jama.2015.7008](https://doi.org/10.1001/jama.2015.7008)
([PMC full text](https://pmc.ncbi.nlm.nih.gov/articles/PMC4664176/))

At age 60, a history of any two of these conditions was associated with about 12 years lower life
expectancy, all three with about 15 years. Each condition carried a similar mortality risk.

| Conditions | Years | Note                                   |
|------------|------:|----------------------------------------|
| 1          |    −6 | approximated as half of two conditions |
| 2          |   −12 |                                        |
| 3          |   −15 |                                        |

## Known limitations

- **Effects are added up independently.** In reality risk factors overlap (e.g. smoking and low
  BMI), so the sum overstates combined effects.
- **Reference groups differ from the baseline.** The baseline is a population average that already
  includes smokers, inactive people and so on, while each study compares against its own reference
  group. For example, the activity bonus is measured against *no* activity, which makes the estimate
  slightly optimistic for most people.
- **Life expectancy at birth instead of at the current age.** Someone who is already 70 has a higher
  expected age at death than a newborn. The next step is to use remaining life expectancy from a
  period life table (e.g. Destatis Sterbetafel). Until then, many older users will see that they
  have outlived the estimate.
- **Ages differ between studies.** The alcohol figures are for age 40 and the condition figures for
  age 60.
- **Few countries.** Only Germany has its own baseline so far. A full table could come from
  the [UN World Population Prospects](https://population.un.org/wpp/).
