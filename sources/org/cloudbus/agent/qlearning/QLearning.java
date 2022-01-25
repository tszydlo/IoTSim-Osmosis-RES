package org.cloudbus.agent.qlearning;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class QLearning {
    int states;
    int actions;
    double[][] Q_matrix;

    double alfa=0.1; //learning_rate
    double gamma=0.8; //discount
    double epsilon=0.2; //epsilon greedy strategy

    int internal_state;

    Random random;

    public QLearning(int states, int actions, boolean rand) {
        this.states = states;
        this.actions = actions;

        Q_matrix = new double[states][actions];

        random = new Random();

        for(int i=0; i<states; i++){
            for(int j=0; j<actions; j++){
                if (rand){
                    Q_matrix[i][j] = random.nextDouble();
                } else {
                    Q_matrix[i][j] = 0;
                }
            }
        }
    }

    public double getAlfa() {
        return alfa;
    }

    public void setAlfa(double alfa) {
        this.alfa = alfa;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void updateState(int state){
        this.internal_state = state;
    }

    private void checkNaN(){
        for(int i=0; i<states; i++){
            for(int j=0; j<actions; j++){
                if (Q_matrix[i][j] != Q_matrix[i][j]){
                    System.out.println("NaN for "+ i + " " + j);
                }
            }
        }
    }

    private int selectMaxIndex(int state){
        double max = Q_matrix[state][0];
        int index = 0;

        for(int i=0; i<actions; i++){
            if (Q_matrix[state][i]>max) {
                max = Q_matrix[state][i];
                index = i;
            }
        }

        return index;
    }

    public int selectAction(){
        return selectMaxIndex(internal_state);
    }

    public int selectActionWithExploration(){
        if (random.nextDouble() > epsilon){
            return selectAction();
        } else {
            return random.nextInt(actions);
        }
    }

    public void update(int action, int newState, double reward){
        double val = Q_matrix[internal_state][action];

        Q_matrix[internal_state][action] = Q_matrix[internal_state][action] + alfa * (
            reward + gamma * Q_matrix[newState][selectMaxIndex(newState)] - Q_matrix[internal_state][action]);

        updateState(newState);

        checkNaN();
    }

    public void setQforAction(int action, double value){
        Q_matrix[internal_state][action] = value;
    }

    public void saveQ(String fname){
        try (FileWriter jsonFileWriter = new FileWriter(fname)){
            Gson gson = new Gson();
            String json = gson.toJson(Q_matrix);
            jsonFileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readQ(String fname){
        try (FileReader jsonFileReader = new FileReader(fname)){

            Gson gson = new Gson();
            Q_matrix = gson.fromJson(jsonFileReader,Q_matrix.getClass());

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: input configuration file not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
