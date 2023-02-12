# Emovo: Emotion Monitoring Mobile APP Based on Speech Recognition Model

## Introduction

This is a final project for AIoT course in Tsinghua University.

Emotional health problems in young people have become the most prevalent of all health problems. 
Emotional problems develop over time. People often do not realize that their emotions are abnormal at the beginning, so they cannot make timely adjustments.

However, there are no proper tools to help people understand their emotional problems, and the existing tools require their own initiative to record and maintain good habits.


This project aims to develop a real-time emotion monitoring system on edge devices to recognize the emotion by analyzing the speaker's voice intonation. People can use this system to understand their emotions and actively cope with them.

![](/ReadmeImg/Userflow.svg)

## Implementation


![](/ReadmeImg/AppMainpageDev.svg)
![](/ReadmeImg/AppStatpageDev.svg)
![](/ReadmeImg/AppOtherpageDev.svg)

## User Study

We explore the effect of gender, content and language on model performance.
![](/ReadmeImg/UserStudyQue.svg)

We invited 7 males and 5 females to join in the test. Each subject speak 6 Chineses sentences and 6 English sentences as shown in the figure.
![](/ReadmeImg/UserStudy.svg)

First, we perform normality test (Agostino and Pearson).
For different sentences, P value varies from 0.052 to 0.477, which means it isn’t a normal  distribution.
So, we choose Wilcoxon test to explore the difference between male and female.
As shown in the page, P value varies from 0.0625 to 1, which means the differences were not statistically significant.

Then we first deploy Wilcoxon test for 132 pairs of sentences, 21 percent p values are lower than 0.05, we believe the content slightly effects the model performance. We emphasize that we cannot differ the effect of content and fine sorted emotion such as happy and exciting.

We finally perform Friedman test. The p value among Chinese sentences and English Sentences are both lower than 0.05, which means the differences were statistically significant.

![](/ReadmeImg/UserStudyAna.svg)

## Teamwork

Yue Sun: Application Developer

Yuang Tong: Emotion Recognition Model Training

Evie Mo: Designer

This project is based on PyTorch template.

Date: 12/28/2021

